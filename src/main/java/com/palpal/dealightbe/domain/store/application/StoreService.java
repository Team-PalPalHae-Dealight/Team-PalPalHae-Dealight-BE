package com.palpal.dealightbe.domain.store.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.palpal.dealightbe.domain.address.application.AddressService;
import com.palpal.dealightbe.domain.member.domain.Member;
import com.palpal.dealightbe.domain.member.domain.MemberRepository;
import com.palpal.dealightbe.domain.store.application.dto.request.StoreCreateReq;
import com.palpal.dealightbe.domain.store.application.dto.response.StoreCreateRes;
import com.palpal.dealightbe.domain.store.domain.Store;
import com.palpal.dealightbe.domain.store.domain.StoreRepository;
import com.palpal.dealightbe.global.error.ErrorCode;
import com.palpal.dealightbe.global.error.exception.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Transactional
@Slf4j
@RequiredArgsConstructor
@Service
public class StoreService {

	private final StoreRepository storeRepository;
	private final MemberRepository memberRepository;
	private final AddressService addressService;

	public StoreCreateRes register(Long memberId, StoreCreateReq req) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> {
				log.warn("GET:READ:NOT_FOUND_MEMBER_BY_ID : {}", memberId);
				throw new EntityNotFoundException(ErrorCode.NOT_FOUND_MEMBER);
			});

		addressService.register(req.addressName(), req.xCoordinate(), req.yCoordinate());

		Store store = StoreCreateReq.toStore(req);
		store.updateMember(member);
		storeRepository.save(store);

		return StoreCreateRes.from(store);
	}
}
