package seedu.files;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

import seedu.common.CommonFiles;
import seedu.data.CarparkList;
import seedu.exception.FileWriteException;
import seedu.exception.InvalidFormatException;
import seedu.exception.NoCarparkFoundException;
import seedu.exception.NoFileFoundException;
import seedu.ui.Ui;

/**
 * Represents the 'favourite' class.
 */
public class Favourite {
    private static ArrayList<String> favouriteList = new ArrayList<>();
    private final FileStorage fileStorage;
    private final String directory;
    private final String file;

    /**
     * Constructor for favourite class.
     *
     * @param directory Directory of the favourite file.
     * @param file Filename of the favourite file.
     */
    public Favourite(String directory, String file) {
        this.fileStorage = new FileStorage(directory, file);
        this.directory = directory;
        this.file = file;
    }

    /**
     * Reads in data from favourite.txt file and saves it to this object.
     *
     * @throws NoFileFoundException If no file found.
     */
    public void updateFavouriteList(CarparkList carparkList)
            throws NoFileFoundException, FileWriteException {
        String content = FileReader.readStringFromTxt(file, directory, true).trim();
        if (content.isEmpty()) {
            return;
        }
        String[] lines = content.split("\\R");
        ArrayList<String> tempArray = new ArrayList<>();
        Collections.addAll(tempArray, lines);
        tempArray = new ArrayList<>(new LinkedHashSet<>(tempArray));
        boolean isValid = ensureValidity(carparkList, tempArray);

        // Don't throw exception and interrupt the flow in the method that calls this one - file should be
        // good to go after writing.
        if (!isValid) {
            writeFavouriteList();
            InvalidFormatException e = new InvalidFormatException("NOTE: There was an issue loading some favourites "
                + "in your " + CommonFiles.FAVOURITE_FILE + " file.\n      The problematic items have been skipped "
                + "and removed from the list.");
            Ui.printError(e);
        }
    }

    /**
     * Makes sure all items in the array are valid items within the {@link CarparkList} class.
     * @param carparkList the carpark list to check against.
     * @param idArray the array with IDs to check validity
     *
     * @return true if all items were valid, false if some invalid items were found.
     */
    private boolean ensureValidity(CarparkList carparkList, ArrayList<String> idArray) {
        favouriteList = new ArrayList<>(idArray);
        boolean isValid = true;
        for (String id : idArray) {
            if (!carparkList.isCarparkValid(id)) {
                favouriteList.remove(id);
                isValid = false;
            } else {
                try {
                    carparkList.findCarpark(id).setFavourite(true);
                } catch (NoCarparkFoundException e) {
                    // Should never trigger (since id should be confirmed valid)
                    assert false : "There is an issue with loading favourites. Please check with the developers.";
                }
            }
        }
        return isValid;
    }

    /**
     * Writes all favourite carpark IDs to favourite.txt file.
     *
     * @throws FileWriteException If unable to write to file.
     */
    public void writeFavouriteList() throws FileWriteException {
        StringBuilder content = new StringBuilder();
        for (String id : favouriteList) {
            content.append(id).append("\n");
        }
        fileStorage.writeDataToFile(content.toString());
    }

    public void replaceFavouriteList(ArrayList<String> content) {
        favouriteList = content;
    }

    public static ArrayList<String> getFavouriteList() {
        return favouriteList;
    }

    public String getFavouriteListString(CarparkList carparkList) throws NoCarparkFoundException {
        StringBuilder content = new StringBuilder();
        for (String id : favouriteList) {
            content.append(Ui.getSeparatorString()).append("\n");
            content.append(carparkList.findCarpark(id).getListViewString()).append("\n");
        }
        return content.toString();
    }
}
