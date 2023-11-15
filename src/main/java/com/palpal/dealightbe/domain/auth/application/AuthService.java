package com.palpal.dealightbe.domain.auth.application;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.auth.application.dto.request.MemberAuthReq;
import com.palpal.dealightbe.domain.auth.application.dto.response.JoinRequireRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.LoginRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.MemberAuthRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.OAuthLoginRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.RequiredUserInfoRes;
import com.palpal.dealightbe.domain.auth.domain.Jwt;
import com.palpal.dealightbe.domain.image.ImageService;
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
	private final ImageService imageService;
	private final Jwt jwt;

	@Transactional(readOnly = true)
	public OAuthLoginRes authenticate(RequiredUserInfoRes requiredUserInfoRes) {
		log.info("사용자 데이터({})를 기반으로 로그인을 진행합니다...", requiredUserInfoRes);
		String provider = requiredUserInfoRes.provider();
		long providerId = requiredUserInfoRes.providerId();
		Optional<Member> optionalMember = memberRepository.findByProviderAndProviderId(provider, providerId);

		if (optionalMember.isEmpty()) {
			log.info("회원가입이 필요하다는 메시지를 반환합니다...");
			String nickname = requiredUserInfoRes.nickName();
			JoinRequireRes joinRequireRes = new JoinRequireRes(provider, providerId, nickname);

			return OAuthLoginRes.from(joinRequireRes);
		}
		LoginRes loginRes = login(optionalMember.get());

		return OAuthLoginRes.from(loginRes);
	}

	public MemberAuthRes signup(MemberAuthReq request) {
		log.info("요청한 데이터로 회원가입을 진행합니다...");
		log.info("요청 데이터 -> Provider: {}, ProviderId: {}, Role: {}",
			request.provider(), request.providerId(), request.role());

		log.info("회원(ProviderId: {}) 유무를 조회합니다...", request.providerId());
		memberRepository.findByProviderAndProviderId(request.provider(), request.providerId())
			.ifPresent(member -> {
				log.warn("POST:READ:ALREADY_EXIST_MEMBER_: PROVIDER({}), PROVIDER_ID({})",
					member.getProvider(), member.getProviderId());
				throw new BusinessException(ErrorCode.ALREADY_EXIST_MEMBER);
			});

		log.info("회원(ProviderId: {})이 가입되어 있지 않아 회원가입을 진행합니다...", request.providerId());
		Member requestMember = createRequestMember(request);
		log.info("회원의 프로필을 기본 이미지(URL: {})로 지정합니다...", MEMBER_DEFAULT_IMAGE_PATH);
		requestMember.updateImage(MEMBER_DEFAULT_IMAGE_PATH);
		Member savedMember = memberRepository.save(requestMember);

		log.info("회원({})의 Role({})을 생성합니다...", savedMember.getProviderId(), request.role());
		List<MemberRole> assignableMemberRoles = createMemberRoles(request, savedMember);
		List<MemberRole> savedMemberRoles = memberRoleRepository.saveAll(assignableMemberRoles);
		log.info("회원({})의 Role을 생성했습니다.", savedMember.getProviderId());
		savedMember.updateMemberRoles(savedMemberRoles);
		log.info("회원가입에 모두 성공했습니다.");

		return createMemberSignupResponse(savedMember);
	}

	@Transactional(readOnly = true)
	public MemberAuthRes reIssueToken(Long providerId, String refreshToken) {
		log.info("사용자(ProviderId:{})의 AccessToken을 재발급합니다.", providerId);
		Member member = findMemberByProviderId(providerId);
		log.info("사용자(Provider: {}, ProviderId: {}, RealName: {})를 조회하는데 성공했습니다.",
			member.getProvider(), member.getProviderId(), member.getRealName());

		log.info("사용자({})의 Access Token을 재발급합니다...", providerId);
		String newAccessToken = jwt.createAccessToken(member);
		log.info("Access Token({}) 재발급에 성공했습니다.", newAccessToken);

		return createMemberAuthRes(member, newAccessToken, refreshToken);
	}

	public void unregister(Long providerId) {
		log.info("사용자(ProviderId:{})의 회원탈퇴를 진행합니다...", providerId);
		Member member = findMemberByProviderId(providerId);
		log.info("사용자(Provider: {}, ProviderId: {}, RealName: {})를 조회하는데 성공했습니다.",
			member.getProvider(), member.getProviderId(), member.getRealName());

		String memberImageUrl = member.getImage();
		if (!memberImageUrl.equals(MEMBER_DEFAULT_IMAGE_PATH)) {
			log.info("사용자(ProviderId:{})의 이미지를 삭제합니다...", providerId);
			imageService.delete(memberImageUrl);
			log.info("이미지(ImageUrl:{}) 삭제에 성공했습니다.", memberImageUrl);
		}

		log.info("사용자(ProviderId:{})의 정보를 삭제합니다...", providerId);
		memberRepository.delete(member);
		log.info("회원탈퇴에 성공했습니다.");
	}

	private LoginRes login(Member member) {
		log.info("사용자(provider: {}, providerId: {})의 로그인을 진행합니다.", member.getProvider(), member.getProviderId());
		Long providerId = member.getProviderId();
		String accessToken = jwt.createAccessToken(member);
		String refreshToken = jwt.createRefreshToken(member);

		return new LoginRes(providerId, accessToken, refreshToken);
	}

	private Member findMemberByProviderId(Long providerId) {
		log.info("사용자(ProviderId:{})정보를 조회합니다...", providerId);
		return memberRepository.findMemberByProviderId(providerId)
			.orElseThrow(() -> {
				log.warn("READ:NOT_FOUND_MEMBER_BY_ID : {}", providerId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});
	}

	private Member createRequestMember(MemberAuthReq request) {
		log.info("요청 정보로 Member 객체를 만듭니다...");
		Address defaultAddress = new Address();
		Member requestMember = MemberAuthReq.toMember(request);
		requestMember.updateAddress(defaultAddress);
		log.info("Member(provider: {}, providerId: {}) 객체를 만드는데 성공했습니다.",
			requestMember.getProvider(), requestMember.getProviderId());

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
		log.info("회원의 Access Token과 Refresh Token을 생성합니다...");
		String accessToken = jwt.createAccessToken(savedMember);
		String refreshToken = jwt.createRefreshToken(savedMember);
		String nickName = savedMember.getNickName();
		log.info("Access Token({}), Refresh Token({}), NickName({})",
			accessToken, refreshToken, nickName);

		return new MemberAuthRes(nickName, accessToken, refreshToken);
	}

	private MemberAuthRes createMemberAuthRes(Member member, String newAccessToken, String refreshToken) {
		String nickName = member.getNickName();
		boolean isRefreshTokenAroundExpiryDate = checkRefreshTokenAroundExpiryDate(refreshToken);
		if (isRefreshTokenAroundExpiryDate) {
			log.info("Refresh Token을 재발급합니다...");
			String newRefreshToken = jwt.createRefreshToken(member);

			log.info("새로 발급한 Refresh Token으로 MemberAuthRes을 생성합니다...");
			return new MemberAuthRes(nickName, newAccessToken, newRefreshToken);
		}

		log.info("기존의 Refresh Token으로 MemberAuthRes을 생성합니다...");
		return new MemberAuthRes(nickName, newAccessToken, refreshToken);
	}

	private boolean checkRefreshTokenAroundExpiryDate(String refreshToken) {
		log.info("Refresh Token의 만료일을 체크합니다...");
		Date expiryDate = jwt.getExpiryDate(refreshToken);
		LocalDateTime expiryDateLocalDateTime = expiryDate.toInstant()
			.atZone(ZoneId.systemDefault()).toLocalDateTime();
		log.info("Refresh Token의 만료일 : {}", expiryDateLocalDateTime);

		LocalDateTime now = LocalDateTime.now();
		log.info("현재시간 : {}", now);

		log.info("현재 시간으로부터 만료일이 10일 이내이면 Refresh Token을 재발급합니다...");
		LocalDateTime timeFromNowAfterTenDays = now.plusDays(10L);
		boolean isRefreshTokenReIssue = timeFromNowAfterTenDays.isAfter(expiryDateLocalDateTime);
		log.info("Refresh Token 재발급 여부 : {}", isRefreshTokenReIssue);

		return isRefreshTokenReIssue;
	}
}
