package specification;
import java.io.File;  
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import exception.DirectoryException;
import exception.InvalidArgumentsExcpetion;
import exception.NamingPolicyException;
import exception.OperationNotAllowed;
import exception.NotFound;
import exception.PathException;
import exception.StorageConnectionException;
import exception.StorageException;
import exception.StoragePathException;
import exception.StorageSizeException;
import exception.UnsupportedFileException;
import fileMetadata.FileMetadata;
import fileMetadata.FileMetadata.FileMetadataBuilder;
import storageInformation.StorageInformation;
import storageManager.StorageManager;

/**
 * Class that is used for initialising Storage.
 * 
 * @author Luka Pavlovic
 * 
 */

public abstract class Storage {
	
    /**
     * Creates a new storage and connects to it upon its creation. 
     * Creation of the storage also requires creation of one more directory which will be root directory for the data and
     * one more file which will hold the information necessary to storage works properly. 
     * After that, it is necessary to call function 'createStorageTreeStructure(dest)' which will create structure which describes storage paths and it's content if some of paths denotes directories
     * @param dest is a path to the new storage. Last name on the path represents a storage name
     * @return true if storage is successfully created, false otherwise
     * @throws NamingPolicyException if parent directory contains a file or a folder with the same name as storage name
     * @throws PathException if path is incorret
     * @throws StorageConnectionException if other storage is already connected
     * @throws StoragePathException if some storage already exists along the path
     */
	public abstract boolean createStorage(String dest) 
			throws NamingPolicyException, PathException, StorageConnectionException, StoragePathException; // mkstrg
	
	/**
	 * Connects to the storage which reside on the path given by src
	 * @param src is path to the existing storage
	 * @return true if successfully connected, false otherwise
	 * @throws NotFound if some directory along the path does not exist
	 * @throws StorageException if given path does not represent the storage
	 * @throws PathException if path is incorrect
	 * @throws StorageConnectionException if storage is not connected
	 */
	public abstract boolean connectToStorage(String src) throws NotFound, StorageException, PathException, StorageConnectionException; // con
	
	
	/**
	 * Disconnects from the currently connected storage
	 * @return true if successfully disconnected, false otherwise
	 */
	public abstract boolean disconnectFromStorage(); // discon
	
	
	/**
	 * Creates directory.  Before creating a file it's necessery to create FileMetadata with 'isDirectory' attribute set to true, which will describe the directory which is intended to create and to call function 'addFileMetadataToStorage(dest, fileMetadata)'
	 * @param dest is a path to the new directory. Last name on the path represents a directory name. 
	 * 		  If the name already exists in the destination folder then the name will be concatenated with a number to make the it unique in the residing directory
	 * @param filesLimit is the maximum number of files and directoris that created directory can hold
	 * @return true if directory is successfully created, false otherwise
	 * @throws StorageSizeException if there is no anymore free space in the storage
	 * @throws DirectoryException if the number of files and folders which parent directory can hold is reached
	 * @throws StorageConnectionException if storage is not connected
	 */
	public abstract boolean createDirectory(String dest, Integer... filesLimit) 
			throws StorageSizeException, DirectoryException, StorageConnectionException; // mkdir
	
	
	/**
	 * Creates a list of directories
	 * @param dest is the path to the the destination folder where the new directories will be created
	 *		  If some of names already exists in the destination folder then the name will be concatenated with a number to make the it unique in the residing directory
	 * @param dirNameAndFilesLimit is a map where keys represents directories names to be created and its values represents maximum number of files and directoris that created directories can hold respectively
	 * @return true if directories are successfully created, false otherwise
	 */
	public boolean createDirectories(String dest, Map<String, Integer> dirNameAndFilesLimit ) { //mkdirs
		try {
			
			for(String name : dirNameAndFilesLimit.keySet()) {
				Integer filesLimit = dirNameAndFilesLimit.get(name);
				createDirectory(dest + File.separator + name, filesLimit);		
			}
			
		} catch (StorageSizeException | DirectoryException | StorageConnectionException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	/**
	 * Creates a file. Before creating a file it's necessery to create FileMetadata with 'isFile' attribute set to true, which will describe the file which is intended to create and to call function 'addFileMetadataToStorage(dest, fileMetadata)'
	 * @param dest is a path to the new directory. Last name on the path represents a directory name. 
	 * 		  If the name already exists in the destination folder then the name will be concatenated with a number to make the it unique in the directory
	 * @return true if successfully connected, false otherwise
	 * @throws StorageSizeException if there is no anymore free space in the storage
	 * @throws UnsupportedFileException if file has extension which storage does not support
	 * @throws DirectoryException if the number of files and folders which parent directory can hold is reached
	 * @throws StorageConnectionException if storage is not connected
	 */
	public abstract boolean createFile(String dest) 
			throws StorageSizeException, UnsupportedFileException, DirectoryException, StorageConnectionException; // mkfile
	
	
	/**
	 * Creates a list of files
	 * @param dest is the path to the the destination folder where the new directories will be created 
	 * @param names is the list of file namse to be created. 
	 * 		  If some of names already exists in the destination folder then the name will be concatenated with a number to make the it unique in the residing directory
	 * @return true if files are successfully created, false otherwise
	 */
	public boolean createFiles(String dest, List<String> names) { // mkfiles
		try {
		
			for(String name : names) 
				createFile(dest + File.separator + name);
		
		} catch (StorageSizeException | UnsupportedFileException | StorageConnectionException | DirectoryException e) {	
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Moves the file or directory to the other destination. Before moving the file or directory it's necessery to call 'moveFileMetadata(src, newDest)' which will move FileMetadata (which describes the file or directory) to the new destination
	 * @param filePath is path to the file to be moved
	 * @param newDest is path to the destination folder
	 * @return true if file or directory is successfully moved, false otherwise
	 * @throws NotFound if file to be moved or destionation folder does not exist
	 * @throws DirectoryException if the number of files and folders which parent directory can hold is reached 
	 * @throws StorageConnectionException if storage is not connected
	 */
	public abstract boolean move(String filePath, String newDest) throws NotFound, DirectoryException, StorageConnectionException; // move 
	
	
	/**
	 * Deletes the file or directory. Before deleting file it's necessery to call function 'removeFileMetadataFromStorage(path)' to remove FileMetadata which describes file or directory which is intended to be deleted
	 * @param filePath is the path to the file or directory to be deleted
	 * @return true if file or directory is successfully deleted, false otherwise
	 * @throws NotFound if the file or directory to be deleted does not exist
	 * @throws StorageConnectionException if storage is not connected
	 */
	public abstract boolean remove(String filePath) throws NotFound, StorageConnectionException; // del
	
	
	/**
	 * Renames the file or directory. Before renaming the file it's necessery to call 'removeFileMetadataFromStorage(path)' to rename FileMetadata which describes file or directory which is intended to be renamed
	 * @param filePath is the path to the file or directory to be renamed
	 * @param newName is the name which will replace the old one
	 * @return true if file or directory is successfully renamed, false otherwise
	 * @throws NotFound if the file or directory to be renamed does not exist
	 * @throws StorageConnectionException if storage is not connected
	 */
	public abstract boolean rename(String filePath, String newName) throws NotFound, StorageConnectionException; // rename
	
	
	/**
	 * Downloads the file or directory
	 * @param filePath is the path to the file or directory to be downloaded
	 * @param downloadDest is the destination folder for downloaded items
	 * @return true if file or directory is successfully downloaded, false otherwise
	 * @throws NotFound if the file or directory to be downloaded does not exist
	 * @throws StorageConnectionException if storage is not connected
	 * @throws PathException if destination path is incorrect
	 * @throws OperationNotAllowed if the destination is located in the storage
	 */
	public abstract boolean download(String filePath, String downloadDest) throws NotFound, StorageConnectionException, PathException, OperationNotAllowed; // download
	
	
	/**
	 * Copies the file or directory. Before coping the file or directory it's necessery to call function 'copyFileMetadata(src, dest)' to copy FileMetadata which describes file or directory which is intended to be copied to the destination dest
	 * @param filePath is the path to the file or directory to be copied
	 * @param dest is the destination folder where the file or directory will be copied
	 * @return true if file or directory is successfully copied, false otherwise
	 * @throws NotFound if file to be copied or destionation folder does not exist
	 * @throws StorageConnectionException if storage is not connected
	 */
	public abstract boolean copyFile(String filePath, String dest) throws NotFound, StorageConnectionException; // copy
	
	
	/**
	 * Writes data to the file. Before writing to the file it's necessery to call function 'writeToFileMetadata(getAbsolutePath(filePath).toString(), text, append)' to set new size FileMetadata which describes file
	 * @param filePath is the path to the file
	 * @param text is the data to be written in the file
	 * @param append if true appends to the existing data in the file, otherwise writes data at the beginning of the file
	 * @return true if data is successfully written in the file, false otherwise
	 * @throws NotFound if the file to be written in does not exist
	 * @throws StorageSizeException if there is no anymore free space in the storage
	 * @throws StorageConnectionException if storage is not connected
	 * @throws OperationNotAllowed if given path does not represent the file
	 */
	public abstract boolean writeToFile(String filePath, String text, boolean append) throws NotFound, StorageSizeException, StorageConnectionException, OperationNotAllowed; //write
	
	
	/**
	 * Checks storage existance along the path
	 * @param path is the path to be checked
	 * @return true if storage exist along the path, false otherwise
	 * @throws PathException if the path is incorrect
	 */
	protected abstract boolean checkStorageExistence(String path) throws PathException;
	
	
	/**
	 * Saves the object to the JSON file
	 * @param obj is the object to be saved
	 */
	protected abstract void saveToJSON(Object obj);
	
	
	/**
	 * Reads the object from the JSON file
	 * @param obj is the object to be read
	 * @param path is the path to JSON file to be read from
	 */
	protected abstract void readFromJSON(Object obj, String path);
	
	
	/**
	 * Changes current directory to the specified one
	 * @param dest is the path to the directory to be changed in
	 * @return true if the directory is successfully changed, false otherwise
	 * @throws NotFound if the directory does not exist
	 * @throws StorageConnectionException if storage is not connected
	 */
	public boolean changeDirectory(String dest) throws NotFound, StorageConnectionException { // cd
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
					
		if(dest.equals("cd..")) {
			if(StorageManager.getInstance().getStorageInformation().getCurrentDirectory().isStorage())
				return true;

			StorageManager.getInstance().getStorageInformation().setCurrentDirectory(StorageManager.getInstance().getStorageInformation().getCurrentDirectory().getParent());
			return true;
		}
								
		FileMetadata directory = getLastFileMetadataOnPath(getRelativePath(dest), StorageManager.getInstance().getStorageInformation().getStorageTreeStructure());
		
		if(directory == null)
			throw new NotFound("Location does not exist!");
		
		StorageManager.getInstance().getStorageInformation().setCurrentDirectory(directory);
		return true;
	}
	
	
	/**
	 * Checks whether the file or directory exist
	 * @param src is the path to the potential file or directory
	 * @return true if the file or directory exist, false otherwise
	 * @throws StorageConnectionException if storage is not connected
	 */
	public boolean find(String src) throws StorageConnectionException { // hit
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");

		return checkPath(getRelativePath(src), StorageManager.getInstance().getStorageInformation().getStorageTreeStructure());
	}
	
	/**
	 * Checks whether the files or directories exists
	 * @param filePaths are the paths to the potential files or directories
	 * @return for every path returns true if the file or directory exist, false otherwise
	 * @throws StorageConnectionException if storage is not connected
	 */
	public Map<String, Boolean> find(List<String> filePaths) throws StorageConnectionException { // hit -l
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		Map<String, Boolean> result = new HashMap<>();
		for(String path : filePaths) 
			result.put(path, find(path));
		
		return result;
	}
	
	/**
	 * Tries to find all destionatios for the file or directory with a specified name
	 * @param name is the name to be searched for
	 * @return list of all destinations which contains specified name
	 * @throws StorageConnectionException if storage is not connected
	 */
	public List<String> findDestinantions(String name) throws StorageConnectionException { // dest
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		List<String> result = new ArrayList<>();
		Map<String, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();
		
		for(String relativePath : storageTreeStracture.keySet()) {
			
			for(FileMetadata f : storageTreeStracture.get(relativePath)) {
				
				Path path = Paths.get(f.getAbsolutePath());
				
				if(path.getFileName().toString().equals(name))
					result.add(f.getAbsolutePath());
					
			}
		}
		
		return result;
	}
	
	/**
	 * Lists all items from the directory that meet the requirements
	 * @param src is the path to the directory
	 * @param onlyDirs if true searches only for directories
	 * @param onlyFiles if true searches only for files
	 * @param searchSubDirecories if true search continues to the subdirectories
	 * @param extension if not null searches only for the items that have given extension
	 * @param prefix if not null searches only for the items that have given prefix
	 * @param sufix if not null searches only for the items that have given sufix
	 * @param subWord if not null searches only for the items that have given subWord
	 * @return returns the map where keys represent the relative paths of directories and values are all items which are found in the directory respectively
	 * @throws NotFound if the directory does not exist
	 * @throws StorageConnectionException if storage is not connected
	 */
	public List<Map<String, List<FileMetadata>>> listDirectory(String src, boolean onlyDirs, boolean onlyFiles, boolean searchSubDirecories, 
				String extension, String prefix, String sufix, String subWord) throws NotFound, OperationNotAllowed, StorageConnectionException { // ls
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");

		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<String, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();	

		FileMetadata directory = getLastFileMetadataOnPath(getRelativePath(src), storageTreeStracture);
		if(directory == null)
			throw new NotFound("Source directory not found!");
		if(!directory.isDirectory())
			throw new OperationNotAllowed("Given path does not represent directory!");
		
		// fix parameters
		if(onlyDirs)
			onlyFiles = false;
		if(prefix !=null && prefix.length()>0) {
			sufix = null;
			subWord = null;
		}
		if(sufix != null && sufix.length()>0)
			subWord = null;
		
		
		List<Map<String, List<FileMetadata>>> result = new ArrayList<Map<String, List<FileMetadata>>>();
		Queue<String> pathQueue = new LinkedList<>();
		Queue<Integer> depthQueue = new LinkedList<>();
		pathQueue.add(directory.getRelativePath());
		depthQueue.add(0);
		
		result.add(new HashMap<>());
		
		// BFS
		while(!pathQueue.isEmpty()) {
			
			String relativePath = pathQueue.poll();
			Integer depth = depthQueue.poll();
			
			Map<String, List<FileMetadata>> map = result.get(depth);			
			map.put(relativePath, storageTreeStracture.get(relativePath));
			
			if(searchSubDirecories==false)
				break;
			
			boolean flag = false;
			
			for(FileMetadata f : storageTreeStracture.get(relativePath)) {
				if(f.isDirectory()) {
					pathQueue.add(f.getRelativePath());
					depthQueue.add(depth + 1);
					
					if(!flag) {
						flag = true;
						result.add(new HashMap<>());
					}
				}
			}
		}
		
		if(onlyDirs || onlyFiles || (extension != null) || (prefix != null) || (sufix != null) || (subWord != null) ) {
			
			
			
			for(int depth = 0 ; depth < result.size() ; depth++) {
				
				Map<String, List<FileMetadata>> map =  result.get(depth);
				
				for(String key : map.keySet()) {
				
					List<FileMetadata> tmp = new ArrayList<>();
					
						for(FileMetadata f : map.get(key)) {
							
							if(onlyDirs && !f.isDirectory())
								continue;
							if(onlyFiles && !f.isFile())
								continue;
							if(extension != null && !f.getName().endsWith(extension))
								continue;
							if(prefix != null && !f.getName().startsWith(prefix))
								continue;
							if(sufix != null && !f.getName().endsWith(sufix))
								continue;
							if(subWord != null && !f.getName().contains(subWord))
								continue;
							
							tmp.add(f);
						}
					
					map.put(key, tmp);
			
				}
				
				if(searchSubDirecories==false)
					break;
				
			}
		}
		
		return result;
	}
	
	/**
	 * Sorts result by the given requirements. If some of the requirements are set to true then that requirements is considered in the sorting process
	 * @param data is the data upon which the sort is applied
	 * @param byName
	 * @param byCreationDate
	 * @param byModificationDate
	 * @param ascending
	 * @param descending
	 * @return returns the map where keys represent the relative paths of directories and values are sorted items in the directory respectively
	 * @throws StorageConnectionException if storage is not connected
	 */
	public List<Map<String, List<FileMetadata>>> resultSort(List<Map<String, List<FileMetadata>>> data, boolean byName, boolean byCreationDate, 
			boolean byModificationDate, boolean ascending, boolean descending) throws StorageConnectionException{ // sort
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		Comparator<FileMetadata> comparator = Comparator.comparing(FileMetadata::getName);
		
		if(byName && byCreationDate && byModificationDate)
			comparator.thenComparing(FileMetadata::getTimeCreated).thenComparing(FileMetadata::getTimeModified);
		
		else if(!byName && byCreationDate && byModificationDate)
			comparator = Comparator.comparing(FileMetadata::getTimeCreated).thenComparing(FileMetadata::getTimeModified);
		
		else if(byName && !byCreationDate && byModificationDate)
			comparator.thenComparing(FileMetadata::getTimeModified);
		
		else if(byName && byCreationDate && !byModificationDate)
			comparator.thenComparing(FileMetadata::getTimeCreated);
		
		else if(byName && !byCreationDate && !byModificationDate)
			comparator = Comparator.comparing(FileMetadata::getName);
		
		else if(!byName && byCreationDate && !byModificationDate)
			comparator = Comparator.comparing(FileMetadata::getTimeCreated);
		
		else if(!byName && !byCreationDate && byModificationDate)
			comparator = Comparator.comparing(FileMetadata::getTimeModified);
		
		
		for(int i = 0 ; i < data.size() ; i++) {
			
			Map<String, List<FileMetadata>> map = data.get(i);
			
			for(String relativePath : map.keySet()) {
				List<FileMetadata> list = map.get(relativePath);
				if(descending) {
					list = list.stream().sorted(comparator).collect(Collectors.toList());
					Collections.reverse(list);
					map.put(relativePath, list);
				}
				else 
					map.put(relativePath, list.stream().sorted(comparator).collect(Collectors.toList()));
			}
		}
	
		return data;
	}
	
	/**
	 * Filters attributes of the data.
	 * @param data is the data upon which filter is applied
	 * @param atributes : 
	 * if atributes[0] is set to true then file ID is included
	 * if atributes[1] is set to true then file name is included
	 * if atributes[2] is set to true then file relative path is included
	 * if atributes[3] is set to true then file absolute path is included
	 * if atributes[4] is set to true then time of creation is included
	 * if atributes[5] is set to true then time of modifivation is included
	 * if atributes[6] is set to true then whether file is file is included
	 * if atributes[7] is set to true then whether file is directory is included 
	 * @param periods:
	 * if periods[0][0] and periods[0][1] are set the then only files which are created between those two periods are included
	 * if periods[1][0] and periods[1][1] are set the then only files which are modified between those two periods are included
	 * @return returns the map where keys represent the relative paths of directories and values are all items with a filtered attributes
	 * @throws InvalidArgumentsExcpetion if some of periods are not valid
	 * @throws StorageConnectionException if storage is not connected
	 */
	public List<Map<String, List<FileMetadata>>> filterAttributes(List<Map<String, List<FileMetadata>>> data, boolean[] atributes, Date[][] periods) 
			throws InvalidArgumentsExcpetion, StorageConnectionException{ // filter
		
		if(StorageManager.getInstance().getStorageInformation().isStorageConnected() == false)
			throw new StorageConnectionException("Storage is currently disconnected! Connection is required.");
		
		Date createdTimeLowerBound = null;
		Date createdTimeUpperBound = null;
		if(periods[0][0] != null && periods[0][1] != null) {
			createdTimeLowerBound = periods[0][0];
			createdTimeUpperBound = periods[0][1];
			
			if(createdTimeLowerBound.after(createdTimeUpperBound))
				throw new InvalidArgumentsExcpetion("Invalid arguments! createdTimeLowerBound > endPeriod");
		}
		
		Date modifedTimeLowerBound = null;
		Date modifiedTimeUpperBound = null;
		if(periods[1][0] != null && periods[1][1] != null) {
			modifedTimeLowerBound = periods[1][0];
			modifiedTimeUpperBound = periods[1][1];
			
			if(modifedTimeLowerBound.after(modifiedTimeUpperBound))
				throw new InvalidArgumentsExcpetion("Invalid arguments! modifedTimeLowerBound > modifiedTimeUpperBound");
		}		
		
		List<Map<String, List<FileMetadata>>> resultClone = new ArrayList<>();
		
		for(int depth = 0 ; depth < data.size() ; depth++) {
			
			Map<String, List<FileMetadata>> map = data.get(depth);
			
			for(String relativePath : map.keySet()) {
			
				List<FileMetadata> filtered = new ArrayList<>();
				
				for(FileMetadata f : map.get(relativePath)) {
					
					if(createdTimeLowerBound != null && createdTimeUpperBound != null) {
						Date time = f.getTimeCreated();
						if(time.before(createdTimeLowerBound) || time.after(createdTimeUpperBound))
							continue;
					}
					if(modifedTimeLowerBound != null && modifiedTimeUpperBound != null) {
						Date time = f.getTimeModified();
						if(time.before(modifedTimeLowerBound) || time.after(modifiedTimeUpperBound))
							continue;
					}
					
					FileMetadataBuilder builder = new FileMetadataBuilder();
					
					if(atributes[0])
						builder.withFileID(f.getFileID());
					if(atributes[1])
						builder.withName(f.getName());
					if(atributes[2])
						builder.withRelativePath(f.getRelativePath());
					if(atributes[3])
						builder.withAbsolutePath(f.getAbsolutePath());
					if(atributes[4])
						builder.withTimeCreated(f.getTimeCreated());
					if(atributes[5])
						builder.withTimeModified(f.getTimeModified());
					if(atributes[6])
						builder.withIsFile(f.isFile());
					if(atributes[7])
						builder.withIsDirectory(f.isDirectory());								
					
					filtered.add(builder.build());
				}
				
				map.put(relativePath, filtered);
			}
			
			resultClone.add(depth, map);
		}
		
		return resultClone;
	}
	
	/**
	 * Sets the storage configuration
	 * @param size is the maximum number of bytes which storage can hold
	 * @param unsupportedFiles are extensions which storage does not support
	 */
	public void setStorageConfiguration(Long size, Set<String> unsupportedFiles) {
		StorageManager.getInstance().getStorageInformation().setStorageSize(size);
		StorageManager.getInstance().getStorageInformation().setUnsupportedFiles(unsupportedFiles);
	}
	
	/**
	 * Creates storage tree structure which describes storage paths and it's content if path denotes a directory
	 * @param dest is path at which storage resides
	 * @return true if storage tree structure is successfilly created, otherwise false
	 */
	protected boolean createStorageTreeStructure(String dest) {
		
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<String, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
		Path path = Paths.get(dest);

		FileMetadata storage = new FileMetadataBuilder()
			.withFileID(storageInformation.getStorageDirectoryID())
			.withName(path.getFileName().toString())
			.withAbsolutePath(dest)
			.withRelativePath(path.getFileName().toString())
			.withTimeCreated(new Date())
			.withTimeModified(new Date())
			.withIsDirectory(true)
			.withIsStorage(true)
			.withStorageSize(storageInformation.getStorageSize())
			.withUnsupportedFiles(storageInformation.getUnsupportedFiles())
			.build();
		
		FileMetadata dataRoot = new FileMetadataBuilder()
			.withFileID(storageInformation.getDatarootDirectoryID())
			.withName(StorageInformation.datarootDirName)
			.withAbsolutePath(storage.getAbsolutePath() + File.separator + StorageInformation.datarootDirName)
			.withRelativePath(storage.getRelativePath() + File.separator + StorageInformation.datarootDirName)
			.withParent(storage)
			.withTimeCreated(new Date())
			.withTimeModified(new Date())
			.withIsDirectory(true)
			.withIsDataRoot(true)
			.build();
	
		FileMetadata strorageInformationJSONfile = new FileMetadataBuilder()
			.withName(StorageInformation.storageInformationJSONFileName)
			.withAbsolutePath(storage.getAbsolutePath() + File.separator + StorageInformation.storageInformationJSONFileName)
			.withRelativePath(storage.getRelativePath() + File.separator + StorageInformation.storageInformationJSONFileName)
			.withParent(storage)
			.withTimeCreated(new Date())
			.withTimeModified(new Date())
			.withIsFile(true)
			.build();

		
		List<FileMetadata> storageAdjacent = new ArrayList<FileMetadata>();
		storageAdjacent.add(dataRoot);
		storageAdjacent.add(strorageInformationJSONfile);
		
		storageTreeStracture.put(storage.getRelativePath(), storageAdjacent);
		storageTreeStracture.put(dataRoot.getRelativePath(), new ArrayList<FileMetadata>());
		
		storageInformation.setStorageDirectory(storage);
		storageInformation.setDatarootDirectory(dataRoot);
		storageInformation.setStorageInformationJSONfile(strorageInformationJSONfile);
		storageInformation.setCurrentDirectory(dataRoot);
		
		saveToJSON(storageInformation);						
		return true;
	}
		
	/**
	 * Adds FileMetadata and places it in the storage tree structure
	 * @param dest is a path to the new FileMetadata. Last name on the path represents a FileMetadata name. 
	 * 		  If the name already exists in the destination folder then the name will be concatenated with a number to make the it unique in the residing directory
	 * @param fileMetadata is the FileMetadat to be added
	 * @param filesLimit if FileMetadata is describes directory, then filesLimit is the maximum number of files and directoris that created directory can hold
	 * @return returns the absolute path of the added FileMetadata
	 * @throws NotFound if some directory along the path does not exist
	 * @throws StorageSizeException if there is no anymore free space in the storage
	 * @throws DirectoryException if the number of files and folders which parent directory can hold is reached
	 * @throws UnsupportedFileException if FileMetadat has extension which storage does not support
	 * @throws OperationNotAllowed if destination does not represent the directory
	 */
	protected String addFileMetadataToStorage(String path, FileMetadata fileMetadata, Integer... filesLimit) 
			throws NotFound, StorageSizeException, DirectoryException, UnsupportedFileException, OperationNotAllowed {
		
		String name = Paths.get(path).getFileName().toString();
		path = Paths.get(path).getParent().toString(); // parent path
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<String, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
		FileMetadata storage = StorageManager.getInstance().getStorageInformation().getStorageDirectory();

		if(storage.getStorageSize() != null) {
			if(storage.getStorageSize() < 1)
				throw new StorageSizeException("Storage size limit has been reached!");
		}

		if(fileMetadata.isFile() && !storage.getUnsupportedFiles().isEmpty()) {
			for(String extension : storage.getUnsupportedFiles()) {
				if(name.endsWith(extension))
					throw new UnsupportedFileException("Unsupported file!");
			}
		}

		FileMetadata parent = getLastFileMetadataOnPath(getRelativePath(path), storageTreeStracture);
		
		// implementiraj da se naprave svi direktorijumi na putanji ako ne postoje
		if(parent == null)
			throw new NotFound("Path is not correct!");
		if(!parent.isDirectory())
			throw new OperationNotAllowed("Given path does not represent directory!");
		
		if(parent.getNumOfFilesLimit() != null) {
			if(parent.getNumOfFilesLimit() < 1)
				throw new DirectoryException("Number of files limit has been reached!");
			
			parent.setNumOfFilesLimit(parent.getNumOfFilesLimit() - 1);
		}
		
		// ako se u direktorijumu vec nalazi fajl sa imenom fajla koji se kreira		
		name = changeNameIfNameExist(parent.getRelativePath(), name);
			
		fileMetadata.setName(name);
		fileMetadata.setSize(0L);
		fileMetadata.setAbsolutePath(parent.getAbsolutePath() + File.separator + name);
		fileMetadata.setRelativePath(parent.getRelativePath() + File.separator + name);
		fileMetadata.setParent(parent);
		fileMetadata.setTimeCreated(new Date());
		fileMetadata.setTimeModified(new Date());
		
		if(fileMetadata.isDirectory()) {
			storageTreeStracture.put(fileMetadata.getRelativePath(), new ArrayList<FileMetadata>());
			
			if(filesLimit.length>0) {
				Map<String, Integer> map = StorageManager.getInstance().getStorageInformation().getDirNumberOfFilesLimit();
				map.put(fileMetadata.getRelativePath(), filesLimit[0]);
			}
		}
		
		storageTreeStracture.get(parent.getRelativePath()).add(fileMetadata);		
		return fileMetadata.getAbsolutePath();
	}
	
	/**
	 * Removes FileMetadata from storage tree structure
	 * @param path is the path at which FileMetadata resides
	 * @return true if FileMetadata is successfully removed, otherwise false
	 * @throws NotFound if the FileMetadata does not exist
	 */
	protected boolean removeFileMetadataFromStorage(String path) throws NotFound {
				
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		Map<String, List<FileMetadata>> storageTreeStracture = storageInformation.getStorageTreeStructure();
								
		FileMetadata file = getLastFileMetadataOnPath(getRelativePath(path), storageTreeStracture);
		
		if(file == null)
			throw new NotFound("Path does not exist!");
		
		storageTreeStracture.get(file.getParent().getRelativePath()).remove(file);
		return true;
	}
	
	/**
	 * Moves FileMetadata to the other destination in the storage tree structure
	 * @param src is the path to the FileMetada to be moved
	 * @param newDest is the path to the destination folder
	 * @return returns the name of the moved FileMetadata
	 * @throws NotFound if FileMetadata to be moved or destionation folder does not exist
	 * @throws DirectoryException if the number of files and folders which parent directory can hold is reached 
	 * @throws OperationNotAllowed if destination folder is a subfolder of the FileMetadata which is intended to be moved
	 */
	protected String moveFileMetadata(String src, String newDest) throws NotFound, DirectoryException, OperationNotAllowed {
		
	
		Map<String, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();		

		FileMetadata srcFile = getLastFileMetadataOnPath(getRelativePath(src), storageTreeStracture);
		FileMetadata destFile = getLastFileMetadataOnPath(getRelativePath(newDest), storageTreeStracture);
		
		if(srcFile == null)
			throw new NotFound("File path not correct!");
		if(destFile == null)
			throw new NotFound("Destination path not correct!");
		if(!destFile.isDirectory())
			throw new DirectoryException("Destination path does not represent the directory!");
		if(destFile.getRelativePath().startsWith(srcFile.getRelativePath())) 
			throw new OperationNotAllowed("The destination folder is a subfolder of the source folder!");
		
		
		if(destFile.getNumOfFilesLimit() != null) {
			if(destFile.getNumOfFilesLimit() < 1)
				throw new DirectoryException("Number of files limit has been reached!");
			
			destFile.setNumOfFilesLimit(destFile.getNumOfFilesLimit() - 1);
		}
		
		// ako se u direktorijumu vec nalazi fajl sa imenom fajla koji se premesta	
		srcFile.setName(changeNameIfNameExist(destFile.getRelativePath(), srcFile.getName()));
		
		if(srcFile.isDirectory()) 
			pathFix(srcFile.getRelativePath(), destFile.getRelativePath() + File.separator + srcFile.getName(), storageTreeStracture);
				
		storageTreeStracture.get(srcFile.getParent().getRelativePath()).remove(srcFile);
		storageTreeStracture.get(destFile.getRelativePath()).add(srcFile);
		srcFile.setParent(destFile);
		srcFile.setAbsolutePath(destFile.getAbsolutePath() + File.separator + srcFile.getName());
		srcFile.setRelativePath(destFile.getRelativePath() + File.separator + srcFile.getName());		
		srcFile.setTimeModified(new Date());

		return srcFile.getName();
	}
	
	
	/**
	 * Renames FileMetadata
	 * @param src is the path to the FileMetadata to be renamed
	 * @param newName is the name which will replace the old one. 
	 * 		  If the newName already exists in the destination folder then the newName will be concatenated with a number to make the it unique in the residing directory
	 * @return returns the newName of the FileMetadata
	 * @throws NotFound if some of the directories along the way does not exist
	 */
	protected String renameFileMetadata(String src, String newName) throws NotFound{
		
		Map<String, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();
		
		FileMetadata file = getLastFileMetadataOnPath(getRelativePath(src), storageTreeStracture);
		
		if(file == null)
			throw new NotFound("File path not correct!");

		newName = changeNameIfNameExist(file.getParent().getRelativePath(), newName);
		
		if(file.isDirectory()) 
			pathFix(file.getRelativePath(), file.getParent().getRelativePath() + File.separator + newName, storageTreeStracture);	
		
		file.setName(newName);
		file.setTimeModified(new Date());
		file.setAbsolutePath(file.getParent().getAbsolutePath() + File.separator + newName);
		file.setRelativePath(file.getParent().getRelativePath() + File.separator + newName);
		file.setTimeModified(new Date());

		return newName;
	}
	
	// mozda je ovde pozeljnije da se odradi BFS...???
	private void pathFix(String oldKey, String newKey, Map<String, List<FileMetadata>> storageTreeStracture) {
		
		List<FileMetadata> list = storageTreeStracture.get(oldKey);
		storageTreeStracture.put(newKey, list);
		storageTreeStracture.remove(oldKey);

		for(FileMetadata f : list) {

			if(f.isDirectory()) 
				pathFix(f.getRelativePath(), newKey + File.separator + f.getName(), storageTreeStracture);

			// newKey je relativna putanja
			f.setAbsolutePath(newKey + File.separator + f.getName());
			f.setRelativePath(newKey + File.separator + f.getName());
		}
	}

	/**
	 * Copies FileMetadata
	 * @param src is the path to FileMetadata to be copied
	 * @param dest is the destination folder where the FileMetadata will be copied
	 * @throws NotFound if FileMetadata to be copied or destionation folder does not exist
	 * @throws DirectoryException if the number of files and folders which parent directory can hold is reached
	 * @throws StorageSizeException if there is no anymore free space in the storage
	 * @throws OperationNotAllowed if destination folder is a subfolder of the FileMetadata to be copied!
	 */
	protected void copyFileMetadata(String src, String dest) throws NotFound, DirectoryException, StorageSizeException, OperationNotAllowed {
		
		Map<String, List<FileMetadata>> storageTreeStracture = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure();		
		
		FileMetadata srcFile = getLastFileMetadataOnPath(getRelativePath(src), storageTreeStracture);
		FileMetadata destDir = getLastFileMetadataOnPath(getRelativePath(dest), storageTreeStracture);				
		
		if(srcFile == null)
			throw new NotFound("File path not correct!");
		if(destDir == null)
			throw new NotFound("Destination path not correct!");
		if(!destDir.isDirectory())
			throw new DirectoryException("Destination path does not represent the directory!");		
		if(destDir.getRelativePath().startsWith(srcFile.getRelativePath())) 
			throw new OperationNotAllowed("The destination folder is a subfolder of the source folder!");
		
		Long storageSize = StorageManager.getInstance().getStorageInformation().getStorageSize();
		if(storageSize != null) {
			if(storageSize - srcFile.getSize() < 0)
				throw new StorageSizeException("Storage size limit has been reached!");
			
			 StorageManager.getInstance().getStorageInformation().setStorageSize(storageSize - srcFile.getSize());
		}
		
		if(destDir.getNumOfFilesLimit() != null) {
			if(destDir.getNumOfFilesLimit() < 1)
				throw new DirectoryException("Number of files limit has been reached!");
			
			destDir.setNumOfFilesLimit(destDir.getNumOfFilesLimit() - 1);
		}
		
		FileMetadata srcFileClone = srcFile.clone();		
		srcFileClone.setName(changeNameIfNameExist(destDir.getRelativePath(), srcFileClone.getName()));

		srcFileClone.setParent(destDir);
		srcFileClone.setAbsolutePath(destDir.getAbsolutePath() + File.separator + srcFileClone.getName());
		srcFileClone.setRelativePath(destDir.getRelativePath() + File.separator + srcFileClone.getName());
		storageTreeStracture.get(destDir.getRelativePath()).add(srcFileClone);		
		
		if(srcFileClone.isDirectory()) 
			pathClone(srcFile.getRelativePath(), srcFileClone.getRelativePath(), srcFileClone, storageTreeStracture);								
	}
	
	private void pathClone(String fromKey, String toKey, FileMetadata parent, Map<String, List<FileMetadata>> storageTreeStracture) {
				
		Queue<String> fromKeys = new LinkedList<>();
		Queue<String> toKeys = new LinkedList<>();
		Queue<FileMetadata> parents = new LinkedList<>();				
				
		for(;;) {	
			
			storageTreeStracture.put(toKey, new ArrayList<>());
			
			for(FileMetadata f : storageTreeStracture.get(fromKey)) {
				
				FileMetadata clone = f.clone();
				clone.setParent(parent);
				clone.setAbsolutePath(parent.getAbsolutePath() + File.separator + clone.getName());
				clone.setRelativePath(parent.getRelativePath() + File.separator + clone.getName());		
				storageTreeStracture.get(toKey).add(clone);		
				
				if(f.isDirectory()) {
					fromKeys.add(f.getRelativePath());
					toKeys.add(clone.getRelativePath());
					parents.add(clone);
				}
					
			}
			
			if(fromKeys.isEmpty())
				return;
			
			fromKey = fromKeys.poll();
			toKey = toKeys.poll();
			parent = parents.poll();
		}		
	}
	
	
	/**
	 * Sets new size to the FileMetadata
	 * @param filePath is the path to the file
	 * @param text is the data to be written
	 * @param append if true adds new bytes to the existing bytes in the file, otherwise set FileMetadata size to the size of the text
	 * @return true if size is set successfully, false otherwise
	 * @throws NotFound if the file to be written in does not exist
	 * @throws OperationNotAllowed if path does not represents file
	 * @throws StorageSizeException if there is no anymore free space in the storage
	 */
	protected boolean writeToFileMetadata(String filePath, String text, boolean append) throws NotFound, OperationNotAllowed, StorageSizeException {

		FileMetadata file = getLastFileMetadataOnPath(getRelativePath(filePath), StorageManager.getInstance().getStorageInformation().getStorageTreeStructure());
		if(file == null)
			throw new NotFound("File path not correct!");
		if(file.isDirectory())
			throw new OperationNotAllowed("Writing not possible! Given path represents directory.");
		
		Long size = text.length() + ((append == true) ? file.getSize() : 0L);
		
		Long storageSize = StorageManager.getInstance().getStorageInformation().getStorageSize();
		if(storageSize != null) {
			if(storageSize - size < 0)
				throw new StorageSizeException("Storage size limit has been reached!");
			
			StorageManager.getInstance().getStorageInformation().setStorageSize(storageSize - size);
		}
		
		file.setSize(size);
		
		return true;
	}

	
	private FileMetadata getLastFileMetadataOnPath(Path path, final Map<String, List<FileMetadata>> storageTreeStracture) {
	
		FileMetadata ans = null;
		Iterator<Path> iterator = path.iterator();		
	
		if(!iterator.hasNext())
			return null;
		String parent = iterator.next().toString();		
		
		while(iterator.hasNext()) {
									
			String nextDirName = iterator.next().toString();
			List<FileMetadata> list = storageTreeStracture.get(parent);
			
			for(int i=0 ; i<list.size() ; i++) {				
				FileMetadata f = list.get(i);					
				if(f.getName().equals(nextDirName)){
					parent += (File.separator + nextDirName);
					ans = f;
					break;
				}
				
				if(i == list.size() - 1)
					return null;
			}			
		}	
		
		return ans;
	}
	
	private boolean checkPath(Path path, final Map<String, List<FileMetadata>> storageTreeStracture) {
		
		Iterator<Path> iterator = path.iterator();		
	
		if(!iterator.hasNext())
			return false;
		String parent = iterator.next().toString();		
				
		while(iterator.hasNext()) {
	
			String nextDirName = iterator.next().toString();
			List<FileMetadata> list = storageTreeStracture.get(parent);
		
			for(int i=0 ; i<list.size() ; i++) {				
				FileMetadata f = list.get(i);							
				if(f.getName().equals(nextDirName)){
					parent += (File.separator + nextDirName);
					break;
				}
				
				if(i == list.size() - 1)
					return false;
			}			
		}	
		
		return true;
	}
	
	protected String changeNameIfNameExist(String destination, String name) {
		
		List<FileMetadata> directoryContent = StorageManager.getInstance().getStorageInformation().getStorageTreeStructure().get(destination);
		String ans = name;
		Integer k = 1;
		
		for(int i = 0 ; i < directoryContent.size() ; i++) {
			
			FileMetadata f = directoryContent.get(i);
			
			if(f.getName().equals(ans)) {
				ans = name + "(" + (k++) + ")";
				i = 0;
			}
		}
		
		return ans;
	}
	
	
	/**
	 * Creates a Path which describes a relative path for the given path
	 * @param path
	 * @return Path
	 */
	protected Path getRelativePath(String path) {
		
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		String dataRootAbsolutePath = storageInformation.getDatarootDirectory().getAbsolutePath();
		String dataRootRelativePath = storageInformation.getDatarootDirectory().getRelativePath();
		Path relativePath = null;

		// racunamo relativnu putanju u odnosu na trenutni direktorijum	
		if(!path.startsWith(dataRootAbsolutePath) && !path.startsWith(dataRootRelativePath)) 
			relativePath = Paths.get(storageInformation.getCurrentDirectory().getRelativePath()).resolve(Paths.get(path));
		
		else if(path.startsWith(dataRootRelativePath)) 
			relativePath = Paths.get(path);
			/* npr. ako je :
								path = storage\dataRootDirectory\dir1\dir2
					 relativePath je = storage\dataRootDirectory\dir1\dir2
			*/
		else if(path.startsWith(dataRootAbsolutePath)){
			path = path.substring(dataRootAbsolutePath.length() + (path.equals(dataRootAbsolutePath) ? 0 : File.separator.length()));
			relativePath = Paths.get(storageInformation.getDatarootDirectory().getRelativePath()).resolve(path);		
			 /* npr. ako je:
							       path = C:\Users\Luka\Desktop\storage\dataRootDirectory\dir1\dir2
				   dataRootAbsolutePath = C:\Users\Luka\Desktop\storage\dataRootDirectory
				   		   novi path je = dir1\dir2
			            relativePath je = storage\dataRootDirectory\dir1\dir2
			 */			
		}	
		
		return relativePath;
	}
	
	
	/**
	 * Creates a Path which describes a absolute path for the given path
	 * @param path
	 * @return Path
	 */
	protected Path getAbsolutePath(String path) {
		
		StorageInformation storageInformation = StorageManager.getInstance().getStorageInformation();
		String dataRootAbsolutePath = storageInformation.getDatarootDirectory().getAbsolutePath();
		String dataRootRelativePath = storageInformation.getDatarootDirectory().getRelativePath();
			
		if(!path.startsWith(dataRootAbsolutePath) && !path.startsWith(dataRootRelativePath))
			path = storageInformation.getCurrentDirectory().getAbsolutePath() + File.separator + path;		
		else if(path.startsWith(dataRootRelativePath)) {
			path = path.substring(dataRootRelativePath.length() + (path.equals(dataRootRelativePath) ? 0 : File.separator.length()));
			path = storageInformation.getDatarootDirectory().getAbsolutePath() + File.separator + path;
		}
		return Paths.get(path);
	}
}
