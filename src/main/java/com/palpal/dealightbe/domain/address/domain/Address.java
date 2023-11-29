package com.palpal.dealightbe.domain.address.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.palpal.dealightbe.global.BaseEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "addresses")
public class Address extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private double xCoordinate;

	private double yCoordinate;

	public Address() {
		this("프로그래머스 강남",
			127.028422526103,
			37.4974495848055);
	}

	@Builder
	public Address(String name, double xCoordinate, double yCoordinate) {
		this.name = name;
		this.xCoordinate = xCoordinate;
		this.yCoordinate = yCoordinate;
	}

	public static Address defaultAddress() {
		return new Address();
	}

	public void updateInfo(Address address) {
		this.name = address.getName();
		this.xCoordinate = address.getXCoordinate();
		this.yCoordinate = address.getYCoordinate();
	}
}
