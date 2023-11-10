package com.palpal.dealightbe.domain.auth.application;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.auth.application.dto.request.MemberAuthReq;
import com.palpal.dealightbe.domain.auth.application.dto.response.LoginRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.MemberAuthRes;
import com.palpal.dealightbe.domain.auth.domain.Jwt;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.member.domain.MemberRole;
import com.palpal.dealightbe.domain.member.domain.MemberRoleRepository;
import com.palpal.dealightbe.domain.member.domain.Role;
import com.palpal.dealightbe.domain.member.domain.RoleRepository;
import com.palpal.dealightbe.domain.member.domain.RoleType;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.BusinessException;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class AuthService {

	private static final String MEMBER_DEFAULT_IMAGE_PATH = "https://team-08-bucket.s3.ap-northeast-2.amazonaws.com/image/member-default-image.png";
	private final MemberRepository memberRepository;
	private final RoleRepository roleRepository;
	private final MemberRoleRepository memberRoleRepository;
	private final Jwt jwt;

	@Transactional(readOnly = true)
	public LoginRes login(OAuth2AuthenticationToken oAuth2AuthenticationToken) {
		String provider = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
		Long providerId = Long.parseLong(oAuth2AuthenticationToken.getPrincipal().getName());

		return memberRepository.findByProviderAndProviderId(provider, providerId)
			.map(member -> {
				log.info("사용자(provider: {}, providerId: {})의 로그인을 진행합니다.", provider, providerId);
				String accessToken = jwt.createAccessToken(member);
				String refreshToken = jwt.createRefreshToken(member);

				return new LoginRes(providerId, accessToken, refreshToken);
			})
			.orElse(null);
	}

	public MemberAuthRes signup(MemberAuthReq request) {
		memberRepository.findByProviderAndProviderId(request.provider(), request.providerId())
			.ifPresent(member -> {
				log.warn("POST:READ:ALREADY_EXIST_MEMBER_: PROVIDER({}), PROVIDER_ID({})",
					member.getProvider(), member.getProviderId());
				throw new BusinessException(ErrorCode.ALREADY_EXIST_MEMBER);
			});

		Member requestMember = createRequestMember(request);
		requestMember.updateImage(MEMBER_DEFAULT_IMAGE_PATH);
		Member savedMember = memberRepository.save(requestMember);

		List<MemberRole> assignableMemberRoles = createMemberRoles(request, savedMember);
		List<MemberRole> savedMemberRoles = memberRoleRepository.saveAll(assignableMemberRoles);

		savedMember.updateMemberRoles(savedMemberRoles);

		return createMemberSignupResponse(savedMember);
	}

	@Transactional(readOnly = true)
	public MemberAuthRes reIssueToken(Long providerId, String refreshToken) {
		log.info("사용자(ProviderId:{})의 AccessToken을 재발급합니다.", providerId);
		Member member = memberRepository.findMemberByProviderId(providerId)
			.orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER));
		String newAccessToken = jwt.createAccessToken(member);

		return createMemberAuthRes(member, newAccessToken, refreshToken);
	}

	private Member createRequestMember(MemberAuthReq request) {
		log.info("요청 정보({})로 Member 객체를 만듭니다...", request);
		Address defaultAddress = new Address();
		Member requestMember = MemberAuthReq.toMember(request);
		requestMember.updateAddress(defaultAddress);
		log.info("Member({}) 객체를 만드는데 성공했습니다.", requestMember);

		return requestMember;
	}

	private List<MemberRole> createMemberRoles(MemberAuthReq request, Member savedMember) {
		log.info("요청 정보(request: {}, Member: {})로 MemberRole을 만듭니다...", request, savedMember);
		RoleType roleType = getRoleType(request);
		Role assignableRole = roleRepository.findByRoleType(roleType)
			.orElseThrow(() -> {
				log.warn("POST:CREATE:NOT_FOUND_ROLE_BY_ROLE_TYPE : {}", roleType);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_ROLE);
			});
		List<MemberRole> assignableMemberRoles = createMemberRoles(savedMember, assignableRole);
		log.info("MemberRole({})를 만드는데 성공했습니다.", assignableMemberRoles);

		return assignableMemberRoles;
	}

	private RoleType getRoleType(MemberAuthReq request) {
		log.info("요청한 RoleType({})을 찾습니다...", request.role());
		String roleFromString = request.role();
		RoleType roleType = RoleType.fromString(roleFromString);
		log.info("요청한 RoleType({})을 찾는데 성공했습니다.", roleType);

		return roleType;
	}

	private List<MemberRole> createMemberRoles(Member member, Role role) {
		MemberRole memberRole = new MemberRole(member, role);
		List<MemberRole> memberRoles = new ArrayList<>();
		memberRoles.add(memberRole);

		return memberRoles;
	}

	private MemberAuthRes createMemberSignupResponse(Member savedMember) {
		String accessToken = jwt.createAccessToken(savedMember);
		String refreshToken = jwt.createRefreshToken(savedMember);
		String nickName = savedMember.getNickName();

		return new MemberAuthRes(nickName, accessToken, refreshToken);
	}

	private MemberAuthRes createMemberAuthRes(Member member, String newAccessToken, String refreshToken) {
		String nickName = member.getNickName();
		boolean isRefreshTokenAroundExpiryDate = checkRefreshTokenAroundExpiryDate(refreshToken);
		if (isRefreshTokenAroundExpiryDate) {
			String newRefreshToken = jwt.createRefreshToken(member);

			return new MemberAuthRes(nickName, newAccessToken, newRefreshToken);
		}

		return new MemberAuthRes(nickName, newAccessToken, refreshToken);
	}

	private boolean checkRefreshTokenAroundExpiryDate(String refreshToken) {
		Date expiryDate = jwt.getExpiryDate(refreshToken);
		LocalDateTime expiryDateLocalDateTime = expiryDate.toInstant()
			.atZone(ZoneId.systemDefault()).toLocalDateTime();
		LocalDateTime timeFromNowAfterTenDays = LocalDateTime.now().plusDays(10L);

		return timeFromNowAfterTenDays.isAfter(expiryDateLocalDateTime);
	}
}
