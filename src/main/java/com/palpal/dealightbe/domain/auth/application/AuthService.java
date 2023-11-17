package com.palpal.dealightbe.domain.auth.application;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.address.domain.Address;
import com.palpal.dealightbe.domain.auth.application.dto.request.MemberSignupAuthReq;
import com.palpal.dealightbe.domain.auth.application.dto.response.MemberAuthRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.OAuthLoginRes;
import com.palpal.dealightbe.domain.auth.application.dto.response.OAuthUserInfoRes;
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
	public OAuthLoginRes authenticate(OAuthUserInfoRes oAuthUserInfoRes) {
		log.info("사용자 데이터({})를 기반으로 로그인을 진행합니다...", oAuthUserInfoRes);

		String provider = oAuthUserInfoRes.provider();
		long providerId = oAuthUserInfoRes.providerId();

		return memberRepository.findByProviderAndProviderId(provider, providerId)
			.map(this::login)
			.map(OAuthLoginRes::from)
			.orElseGet(() -> {
				log.info("회원가입이 필요하다는 메시지를 반환합니다...");

				return OAuthLoginRes.from(oAuthUserInfoRes);
			});
	}

	public MemberAuthRes signup(MemberSignupAuthReq request) {
		log.info("요청한 데이터(Provider: {}, ProviderId: {})로 회원가입을 진행합니다...",
			request.provider(), request.providerId());
		log.info("회원(ProviderId: {}) 유무를 조회합니다...", request.providerId());

		memberRepository.findByProviderAndProviderId(request.provider(), request.providerId())
			.ifPresent(member -> {
				log.warn("POST:READ:ALREADY_EXIST_MEMBER_: PROVIDER({}), PROVIDER_ID({})",
					member.getProvider(), member.getProviderId());
				throw new BusinessException(ErrorCode.ALREADY_EXIST_MEMBER);
			});

		log.info("회원(ProviderId: {})이 가입되어 있지 않아 회원가입을 진행합니다...", request.providerId());

		Member requestMember = MemberSignupAuthReq.toMember(request);

		setDefaultAddress(requestMember);
		setDefaultImageUrl(requestMember);
		Member savedMember = memberRepository.save(requestMember);

		List<MemberRole> assignableMemberRoles = createMemberRoles(RoleType.ROLE_MEMBER, savedMember);
		List<MemberRole> savedMemberRoles = memberRoleRepository.saveAll(assignableMemberRoles);

		savedMember.updateMemberRoles(savedMemberRoles);

		String accessToken = jwt.createAccessToken(savedMember);
		String refreshToken = jwt.createRefreshToken(savedMember);

		log.info("회원가입에 모두 성공했습니다.");

		return createMemberAuthRes(savedMember, accessToken, refreshToken);
	}

	public void unregister(Long providerId) {
		log.info("사용자(ProviderId:{})의 회원탈퇴를 진행합니다...", providerId);

		Member member = findMemberByProviderId(providerId);

		log.info("사용자({})를 조회하는데 성공했습니다.", member);

		if (!member.hasSameImage(MEMBER_DEFAULT_IMAGE_PATH)) {
			String memberImage = member.getImage();
			log.info("사용자(ProviderId:{})의 이미지({})를 삭제합니다...", providerId, memberImage);

			imageService.delete(memberImage);

			log.info("이미지 삭제에 성공했습니다.");
		}

		log.info("사용자(ProviderId:{})의 정보를 삭제합니다...", providerId);

		memberRepository.delete(member);

		log.info("회원탈퇴에 성공했습니다.");
	}

	@Transactional(readOnly = true)
	public MemberAuthRes reissueToken(Long providerId, String refreshToken) {
		log.info("사용자(ProviderId:{})의 AccessToken을 재발급합니다.", providerId);

		Member member = findMemberByProviderId(providerId);

		log.info("사용자({})를 조회하는데 성공했습니다.", member);

		log.info("사용자({})의 Access Token을 재발급합니다...", providerId);

		String newAccessToken = jwt.createAccessToken(member);

		log.info("Access Token({}) 재발급에 성공했습니다.", newAccessToken);

		if (checkRefreshTokenAroundExpiryDate(refreshToken)) {
			log.info("사용자({})의 Refresh Token을 재발급합니다...", providerId);

			String newRefreshToken = jwt.createRefreshToken(member);

			log.info("Refresh Token 재발급에 성공했습니다.");

			return createMemberAuthRes(member, newAccessToken, newRefreshToken);
		}

		return createMemberAuthRes(member, newAccessToken, refreshToken);
	}

	public MemberAuthRes updateMemberRoleToStore(Long providerId) {
		Member member = memberRepository.findMemberByProviderId(providerId)
			.orElseThrow(() -> {
				log.warn("READ:NOT_FOUND_MEMBER_BY_PROVIDER_ID : {}", providerId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		List<MemberRole> assignableMemberRoles = createMemberRoles(RoleType.ROLE_STORE, member);
		List<MemberRole> savedMemberRoles = memberRoleRepository.saveAll(assignableMemberRoles);
		member.updateMemberRoles(savedMemberRoles);

		String accessToken = jwt.createAccessToken(member);
		String refreshToken = jwt.createRefreshToken(member);

		return createMemberAuthRes(member, accessToken, refreshToken);
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

	private MemberAuthRes login(Member member) {
		log.info("사용자(provider: {}, providerId: {})의 로그인을 진행합니다.", member.getProvider(), member.getProviderId());

		String accessToken = jwt.createAccessToken(member);
		String refreshToken = jwt.createRefreshToken(member);

		return createMemberAuthRes(member, accessToken, refreshToken);
	}

	private void setDefaultAddress(Member newMember) {
		log.info("새로운 회원에게 기본 위치를 부여합니다...");
		Address defaultAddress = new Address();
		newMember.updateAddress(defaultAddress);
		log.info("기본 위치 지정이 완료됐습니다.");
	}

	private void setDefaultImageUrl(Member newMember) {
		log.info("새로운 회원(Provider: {}, ProviderId: {})에게 기본 이미지(Url: {})로 지정합니다...",
			newMember.getProvider(), newMember.getProviderId(), MEMBER_DEFAULT_IMAGE_PATH);
		newMember.updateImage(MEMBER_DEFAULT_IMAGE_PATH);
		log.info(" 기본 이미지 지정이 완료됐습니다.");
	}

	private List<MemberRole> createMemberRoles(RoleType roleType, Member member) {
		log.info("회원(ProviderId: {})의 Role({})을 생성합니다...", member.getProviderId(), roleType.getRole());
		Role assignableRole = roleRepository.findByRoleType(roleType)
			.orElseThrow(() -> {
				log.warn("POST:CREATE:NOT_FOUND_ROLE_BY_ROLE_TYPE");
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_ROLE);
			});
		MemberRole memberRole = new MemberRole(member, assignableRole);
		List<MemberRole> assignableMemberRoles = new ArrayList<>();
		assignableMemberRoles.add(memberRole);
		log.info("Role를 만드는데 성공했습니다.");

		return assignableMemberRoles;
	}

	private MemberAuthRes createMemberAuthRes(Member member, String accessToken, String refreshToken) {
		log.info("회원인증 성공 메시지에 필요한 데이터를 생성합니다...");
		Long userId = member.getProviderId();
		String roles = getMemberRoles(member);
		log.info("회원인증 성공 메시지를 만드는데 완료했습니다.");

		return new MemberAuthRes(userId, roles, accessToken, refreshToken);
	}

	private String getMemberRoles(Member member) {
		log.info("회원({})으로부터 Role 데이터를 가져옵니다...", member);

		List<MemberRole> memberRoles = member.getMemberRoles();
		String roles = memberRoles.stream()
			.map(memberRole -> memberRole.getRole().getType().getRole())
			.collect(Collectors.joining(","));

		log.info("Role({}) 데이터를 가져오는 것을 완료했습니다.", roles);

		return roles;
	}

	private Member findMemberByProviderId(Long providerId) {
		log.info("사용자(ProviderId:{})정보를 조회합니다...", providerId);
		return memberRepository.findMemberByProviderId(providerId)
			.orElseThrow(() -> {
				log.warn("READ:NOT_FOUND_MEMBER_BY_PROVIDER_ID : {}", providerId);
				return new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});
	}
}
