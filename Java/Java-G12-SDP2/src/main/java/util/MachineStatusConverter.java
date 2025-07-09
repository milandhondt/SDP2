package util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MachineStatusConverter implements AttributeConverter<MachineStatus, String>
{

	@Override
	public String convertToDatabaseColumn(MachineStatus status)
	{
		if (status == null)
		{
			return null;
		}
		return status.name();
	}

	@Override
	public MachineStatus convertToEntityAttribute(String dbData)
	{
		if (dbData == null)
		{
			return null;
		}
		return MachineStatus.valueOf(dbData);
	}
}