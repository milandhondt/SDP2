package domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import repository.GenericDao;
import repository.GenericDaoJpa;

/**
 * Controller class responsible for managing {@link FileInfo} entities. Provides
 * operations to retrieve, save, delete, and update file information related to
 * maintenance records.
 */
public class FileInfoController
{

	private final GenericDao<FileInfo> fileInfoDao;

	/**
	 * Constructs a new FileInfoController using a GenericDaoJpa for FileInfo.
	 */
	public FileInfoController()
	{
		this.fileInfoDao = new GenericDaoJpa<>(FileInfo.class);
	}

	/**
	 * Returns the DAO used for FileInfo database operations.
	 * 
	 * @return the fileInfo DAO instance
	 */
	protected GenericDao<FileInfo> getFileInfoDao()
	{
		return fileInfoDao;
	}

	/**
	 * Retrieves a list of all files associated with a specific maintenance ID.
	 * 
	 * @param maintenanceId the ID of the maintenance record
	 * @return list of FileInfo linked to the specified maintenance
	 */
	public List<FileInfo> getFilesForMaintenance(int maintenanceId)
	{
		return fileInfoDao.findAll().stream()
				.filter(file -> file.getMaintenance() != null && file.getMaintenance().getId() == maintenanceId)
				.collect(Collectors.toList());
	}

	/**
	 * Saves a new file record to the database. Starts and commits a transaction
	 * around the insert operation.
	 * 
	 * @param fileInfo the FileInfo entity to save
	 */
	public void saveFile(FileInfo fileInfo)
	{
		fileInfoDao.startTransaction();
		fileInfoDao.insert(fileInfo);
		fileInfoDao.commitTransaction();
	}

	/**
	 * Deletes a file record from the database. Starts and commits a transaction
	 * around the delete operation.
	 * 
	 * @param fileInfo the FileInfo entity to delete
	 */
	public void deleteFile(FileInfo fileInfo)
	{
		fileInfoDao.startTransaction();
		fileInfoDao.delete(fileInfo);
		fileInfoDao.commitTransaction();
	}

	/**
	 * Retrieves the binary content of a given file.
	 * 
	 * @param fileInfo the file entity
	 * @return the content as a byte array
	 */
	public byte[] getFileContent(FileInfo fileInfo)
	{
		return fileInfo.getContent();
	}

	/**
	 * Reads file content from the given {@link File} and saves it into the provided
	 * {@link FileInfo} entity, then persists the entity. Updates the file size and
	 * upload timestamp.
	 * 
	 * @param file     the file to read content from
	 * @param fileInfo the file entity to update and save
	 * @throws IOException if an I/O error occurs reading the file
	 */
	public void saveFileContent(File file, FileInfo fileInfo) throws IOException
	{
		try (FileInputStream fis = new FileInputStream(file))
		{
			byte[] content = fis.readAllBytes();
			fileInfo.setContent(content);
			fileInfo.setSize(content.length);
			fileInfo.setUploadDate(java.time.LocalDateTime.now());
			saveFile(fileInfo);
		}
	}

	/**
	 * Saves the provided byte array content into the {@link FileInfo} entity and
	 * persists it. Updates the file size and upload timestamp.
	 * 
	 * @param content  the file content as a byte array
	 * @param fileInfo the file entity to update and save
	 */
	public void saveFileContent(byte[] content, FileInfo fileInfo)
	{
		fileInfo.setContent(content);
		fileInfo.setSize(content.length);
		fileInfo.setUploadDate(java.time.LocalDateTime.now());
		saveFile(fileInfo);
	}
}
