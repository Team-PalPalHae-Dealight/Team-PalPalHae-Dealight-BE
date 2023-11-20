package com.palpal.dealightbe.domain.auth.application.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public record MemberNickNameCheckReq(
	@NotBlank(message = "닉네임은 비어있을 수 없습니다.")
	@Pattern(regexp = "^[a-zA-Z0-9가-힣]+$\n",
		message = "닉네임은 한글, 알파벳, 숫자만 사용할 수 있습니다.")
	String nickName
) {

}
