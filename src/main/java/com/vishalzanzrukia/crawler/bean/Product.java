package com.vishalzanzrukia.crawler.bean;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@code Product} class
 * 
 * @author VishalZanzrukia
 */
public class Product {

	private String pId;

	private String name;

	private String description;

	private String keywords;

	private String barcode;

	private BigDecimal price;

	private Integer weight;

	private List<Integer> dimensions;

	public Product(@JsonProperty("pId") final String pId, @JsonProperty("name") final String name,
			@JsonProperty("description") final String description, @JsonProperty("keywords") final String keywords,
			@JsonProperty("barcode") final String barcode, @JsonProperty("price") final BigDecimal price,
			@JsonProperty("weight") final Integer weight, @JsonProperty("dimensions") final List<Integer> dimensions) {
		this.pId = pId;
		this.name = name;
		this.description = description;
		this.keywords = keywords;
		this.barcode = barcode;
		this.price = price;
		this.weight = weight;
		this.dimensions = dimensions;
	}

	public String getpId() {
		return pId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getKeywords() {
		return keywords;
	}

	public String getBarcode() {
		return barcode;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public Integer getWeight() {
		return weight;
	}

	public List<Integer> getDimensions() {
		return dimensions;
	}
}
