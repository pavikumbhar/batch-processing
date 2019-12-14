package com.pavikumbhar.batchprocessing.processor;

import org.springframework.batch.item.ItemProcessor;

import com.pavikumbhar.batchprocessing.model.DBInformationDto;
import com.pavikumbhar.batchprocessing.model.PartialInformationDto;

public class DatabaseToDatabaseItemProcessor implements ItemProcessor<DBInformationDto, PartialInformationDto> {
    
    @Override
    public PartialInformationDto process(DBInformationDto item) throws Exception {
        final String id = item.getId();
        final String title = item.getTitle().toUpperCase();
        
        final PartialInformationDto transformedPartialInformationDto = new PartialInformationDto();
        transformedPartialInformationDto.setId(id);
        transformedPartialInformationDto.setTitle(title);
        
        return transformedPartialInformationDto;
        
    }
}
