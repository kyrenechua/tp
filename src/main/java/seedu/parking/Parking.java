package seedu.parking;

import static seedu.common.CommonFiles.API_JSON_DIRECTORY;
import static seedu.common.CommonFiles.API_KEY_FILE;
import static seedu.common.CommonFiles.CARPARK_LIST_DIRECTORY;
import static seedu.common.CommonFiles.CARPARK_LIST_FILE;
import static seedu.common.CommonFiles.FAVOURITE_DIRECTORY;
import static seedu.common.CommonFiles.FAVOURITE_FILE;
import static seedu.common.CommonFiles.LTA_JSON_FILE;

import java.util.Objects;

import seedu.api.Api;
import seedu.commands.Auth;
import seedu.commands.Favourite;
import seedu.commands.Find;
import seedu.commands.Search;
import seedu.common.CommonFiles;
import seedu.data.Carpark;
import seedu.data.CarparkList;
import seedu.exception.DuplicateCarparkException;
import seedu.exception.FileWriteException;
import seedu.exception.NoCarparkFoundException;
import seedu.exception.NoCommandArgumentException;
import seedu.exception.NoFileFoundException;
import seedu.exception.ParkingException;
import seedu.exception.UnneededArgumentsException;
import seedu.files.FileReader;
import seedu.files.FileStorage;
import seedu.parser.Command;
import seedu.parser.Parser;
import seedu.parser.search.Sentence;
import seedu.ui.Ui;

/**
 * Main class of the program.
 */
public class Parking {
    private static CarparkList carparkList;

    /**
     * Main entry-point for the java.duke.Duke application.
     */
    public static void main(String[] args) {
        Parser parser = new Parser();
        Find find = new Find();
        Ui ui = new Ui();
        ui.greetUser();
        Auth auth = new Auth();
        Api api = new Api(LTA_JSON_FILE, API_JSON_DIRECTORY);
        Favourite favourite = new Favourite(FAVOURITE_DIRECTORY, FAVOURITE_FILE);

        try {
            favourite.updateFavouriteList();
        } catch (FileWriteException e) {
            ui.showUpdateFavouriteError();
        } catch (NoFileFoundException e) {
            ui.printError(e);
        }
        try {
            api.loadApiKey(API_KEY_FILE, API_JSON_DIRECTORY, true);
            api.syncFetchData();
            ui.print("Fetching data from API successful!");
        } catch (ParkingException e) {
            ui.print(e.getMessage());
        }

        // Load file from json
        ui.showLoadingDataMessage();
        try {
            carparkList = FileReader.loadCarparkListFromTxt(CARPARK_LIST_FILE, CARPARK_LIST_DIRECTORY);
            CarparkList newCarparkList = new CarparkList(CommonFiles.LTA_FILE_PATH, CommonFiles.LTA_BACKUP_FILE_PATH);
            carparkList.update(newCarparkList);
            FileStorage.saveCarparkList(carparkList);
            ui.showLoadingDataSuccess();
        } catch (ParkingException e) {
            ui.printError(e);
        }

        // Main loop for user to use program
        boolean isExit = false;
        while (!isExit) {
            String input = ui.getCommand();
            Command command;
            try {
                command = parser.parseInputString(input);
            } catch (ParkingException e) {
                ui.printError(e);
                continue;
            }
            switch (Objects.requireNonNull(command)) {
            case EXIT:
                ui.showByeMessage();
                isExit = true;
                break;
            case FIND:
                try {
                    String carparkID = find.getCarparkId(input);
                    Carpark carpark = carparkList.findCarpark(carparkID);
                    ui.print(carpark.getDetailViewString());
                } catch (NoCommandArgumentException | NoCarparkFoundException | UnneededArgumentsException exception) {
                    ui.printError(exception);
                }
                break;
            case UPDATE:
                try {
                    //fetch api
                    api.syncFetchData();
                    //update json
                    carparkList = new CarparkList(CommonFiles.LTA_FILE_PATH, CommonFiles.LTA_BACKUP_FILE_PATH);
                    ui.showUpdateDataSuccess();
                } catch (ParkingException e) {
                    ui.printError(e);
                } finally {
                    System.out.println("Update data terminated"); // Debug line
                }
                break;
            case AUTH:
                try {
                    String[] words = input.trim().split("\\s+", 2);
                    if (api.isApiValid(words[1])) {
                        auth.saveApiKey(input);
                        ui.showApiKeySaved();
                    }
                } catch (ParkingException e) {
                    ui.printError(e);
                }
                break;
            case LIST:
                ui.print(carparkList.toString());
                break;
            case SEARCH:
                Sentence searchQuery = new Sentence(Parser.splitCommandArgument(input)[1]);
                ui.print(Search.runSearch(carparkList, searchQuery).getSearchListString());
                carparkList.resetBoldForAllCarparks();
                break;
            case FAVOURITE:
                try {
                    String carparkID = find.getCarparkId(input);
                    if (carparkID.equalsIgnoreCase("list")) {
                        ui.print(favourite.showList());
                    } else {
                        Carpark carpark = carparkList.findCarpark(carparkID);
                        favourite.setFavourite(carparkID);
                        ui.showFavouriteAddSuccess(carparkID);
                    }
                } catch (FileWriteException | UnneededArgumentsException | NoCommandArgumentException
                         | NoCarparkFoundException | DuplicateCarparkException e) {
                    ui.printError(e);
                }
                break;
            case UNFAVOURITE:
                try {
                    String carparkId = favourite.getCarparkId(input);
                    favourite.setUnfavourite(carparkId);
                    ui.showUnfavouriteSuccess(carparkId);
                } catch (FileWriteException | UnneededArgumentsException | NoCommandArgumentException
                         | NoCarparkFoundException e) {
                    ui.printError(e);
                }
                break;
            default:
                ui.showInvalidCommandError();
                break;
            }
        }
    }
}
