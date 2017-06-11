package com.vishalzanzrukia.crawler.bean;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@code Product} class
 * 
 * @author VishalZanzrukia
 */
@Document(indexName = "productIndex", type = "product")
public class Product {

	@Id
	@MultiField(mainField = @Field(type = FieldType.String), otherFields = {
			@InnerField(index = FieldIndex.not_analyzed, suffix = "testSuffix", type = FieldType.String)})
	private String pId;

	@Field(type = FieldType.String)
	private String name;

	@Field(type = FieldType.String)
	private String description;

	@Field(type = FieldType.String)
	private String keywords;

	@Field(type = FieldType.String)
	private String barcode;

	@Field(type = FieldType.String)
	private BigDecimal price;

	@Field(type = FieldType.Integer)
	private Integer weight;

	@Field(type = FieldType.Integer)
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
