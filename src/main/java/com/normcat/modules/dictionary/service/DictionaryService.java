package com.normcat.modules.dictionary.service;

import com.normcat.modules.dictionary.dto.request.CreateAttributeAliasRequest;
import com.normcat.modules.dictionary.dto.request.CreateAttributeRequest;
import com.normcat.modules.dictionary.dto.request.CreateAttributeValueAliasRequest;
import com.normcat.modules.dictionary.dto.request.CreateAttributeValueRequest;
import com.normcat.modules.dictionary.dto.response.AttributeAliasResponse;
import com.normcat.modules.dictionary.dto.response.AttributeResponse;
import com.normcat.modules.dictionary.dto.response.AttributeValueAliasResponse;
import com.normcat.modules.dictionary.dto.response.AttributeValueResponse;

import java.util.List;

public interface DictionaryService {

    AttributeResponse createAttribute(CreateAttributeRequest request);

    AttributeResponse getAttributeById(Long id);

    List<AttributeResponse> getAllAttributes();

    AttributeValueResponse addAttributeValue(Long attributeId, CreateAttributeValueRequest request);

    List<AttributeValueResponse> getAttributeValues(Long attributeId);

    AttributeAliasResponse addAttributeAlias(Long attributeId, CreateAttributeAliasRequest request);

    List<AttributeAliasResponse> getAttributeAliases(Long attributeId);

    AttributeValueAliasResponse addAttributeValueAlias(Long attributeId, Long valueId, CreateAttributeValueAliasRequest request);

    List<AttributeValueAliasResponse> getAttributeValueAliases(Long attributeId, Long valueId);
}
