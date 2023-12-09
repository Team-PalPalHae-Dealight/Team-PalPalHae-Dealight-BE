package com.palpal.dealightbe.domain.item.domain;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.palpal.dealightbe.domain.store.domain.DocumentStatus;
import com.palpal.dealightbe.domain.store.domain.UpdatedStore;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Entity
@Table(name = "updated_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpdatedItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private int stock;
	private int discountPrice;

	private int originalPrice;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id")
	private UpdatedStore store;

	@Enumerated(EnumType.STRING)
	private DocumentStatus documentStatus = DocumentStatus.READY;

	@Builder
	public UpdatedItem(Long id, String name, int stock, int discountPrice, int originalPrice, UpdatedStore store) {
		this.id = id;
		this.name = name;
		this.stock = stock;
		this.discountPrice = discountPrice;
		this.originalPrice = originalPrice;
		this.store = store;
	}

	public static UpdatedItem from(Item item) {
		return UpdatedItem.builder()
			.name(item.getName())
			.stock(item.getStock())
			.discountPrice(item.getDiscountPrice())
			.originalPrice(item.getOriginalPrice())
			.build();
	}

	public void updateDocumentStatus(DocumentStatus status) {
		this.documentStatus = status;
	}

	public void updateStore(UpdatedStore updatedStore) {
		this.store = updatedStore;
	}

	public void markAsDone() {
		this.documentStatus = DocumentStatus.DONE;
	}
}
