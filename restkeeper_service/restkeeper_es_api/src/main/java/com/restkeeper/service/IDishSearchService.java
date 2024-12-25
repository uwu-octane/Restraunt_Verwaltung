package com.restkeeper.service;

import com.restkeeper.entity.DishEs;
import com.restkeeper.entity.SearchResult;

public interface IDishSearchService {

    //query by product code and product type
    SearchResult<DishEs> searchAllByCode(String code, int type, int pageNum, int pageSize);

    //query by code
    SearchResult<DishEs> searchCuisineByCode(String code, int pageNum, int pageSize);

    //query by name
    SearchResult<DishEs> searchCuisineByName(String name, int type, int pageNum, int pageSize);
	
}
