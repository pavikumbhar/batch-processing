package com.pravinkumbhar.batchprocessing.reader;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.pravinkumbhar.batchprocessing.model.DBInformationDto;

public class DBInformationRowMapper implements RowMapper<DBInformationDto> {

    @Override
    public DBInformationDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        DBInformationDto result = new DBInformationDto();
        result.setId(rs.getString("ID"));
        result.setTitle(rs.getString("TITLE"));
        result.setDescription(rs.getString("DESCRIPTION"));
        return result;
    }

}
