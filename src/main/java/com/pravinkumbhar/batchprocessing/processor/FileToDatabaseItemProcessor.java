package com.pravinkumbhar.batchprocessing.processor;

import org.springframework.batch.item.ItemProcessor;

import com.pravinkumbhar.batchprocessing.model.InformationDto;

public class FileToDatabaseItemProcessor implements ItemProcessor<InformationDto, InformationDto> {
    
    @Override
    public InformationDto process(final InformationDto informationDto) throws Exception {
        
        final String id = informationDto.getId();
        final String title = informationDto.getTitle();
        final String description = informationDto.getDescription();
        return new InformationDto(id, title, description);
    }
    
}