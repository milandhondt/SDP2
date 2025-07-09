package util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RoleListConverter implements AttributeConverter<List<Role>, String>
{

	@Override
	public String convertToDatabaseColumn(List<Role> roles)
	{
		if (roles == null || roles.isEmpty())
			return "[]";
		return roles.stream().map(Role::name).collect(Collectors.joining(",", "[", "]"));
	}

	@Override
	public List<Role> convertToEntityAttribute(String dbData)
	{
		if (dbData == null || dbData.isEmpty())
			return Collections.emptyList();
		dbData = dbData.replace("[", "").replace("]", "").replace("\"", "");
		return Arrays.stream(dbData.split(",")).map(String::trim).map(Role::valueOf).collect(Collectors.toList());
	}

}
