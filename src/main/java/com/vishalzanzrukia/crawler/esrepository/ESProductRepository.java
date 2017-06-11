package com.vishalzanzrukia.crawler.esrepository;

import org.springframework.data.elasticsearch.repository.support.SimpleElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.vishalzanzrukia.crawler.bean.Product;

@Repository
public class ESProductRepository extends SimpleElasticsearchRepository<Product> {

}
