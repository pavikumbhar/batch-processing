package com.pavikumbhar.batchprocessing.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DBInformationDto {
    
    private String id;
    private String title;
    private String description;

}
