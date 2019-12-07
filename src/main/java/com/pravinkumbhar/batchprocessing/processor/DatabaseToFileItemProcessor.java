package com.pravinkumbhar.batchprocessing.processor;

import org.springframework.batch.item.ItemProcessor;

import com.pravinkumbhar.batchprocessing.model.DBInformationDto;

public class DatabaseToFileItemProcessor implements ItemProcessor<DBInformationDto, DBInformationDto> {

    @Override
    public DBInformationDto process(DBInformationDto informationDto) throws Exception {

        final String id = informationDto.getId();
        final String title = informationDto.getTitle();
        final String description = informationDto.getDescription();
        return new DBInformationDto(id, title, description);

    }

}
