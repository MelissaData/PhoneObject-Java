import com.melissadata.*;
import java.io.*;

public class MelissaPhoneObjectWindowsJava {

  public static void main(String args[]) throws IOException {
    // Variables
    String[] arguments = ParseArguments(args);
    String license = arguments[0];
    String testPhone = arguments[1];
    String dataPath = arguments[2];

    RunAsConsole(license, testPhone, dataPath);
  }

  public static String[] ParseArguments(String[] args) {
    String license = "", testPhone = "", dataPath = "";
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--license") || args[i].equals("-l")) {
        if (args[i + 1] != null) {
          license = args[i + 1];
        }
      }
      if (args[i].equals("--phone") || args[i].equals("-p")) {
        if (args[i + 1] != null) {
          testPhone = args[i + 1];
        }
      }
      if (args[i].equals("--dataPath") || args[i].equals("-d")) {
        if (args[i + 1] != null) {
          dataPath = args[i + 1];
        }
      }
    }
    return new String[] { license, testPhone, dataPath };

  }

  public static void RunAsConsole(String license, String testPhone, String dataPath) throws IOException {
    System.out.println("\n\n=========== WELCOME TO MELISSA PHONE OBJECT WINDOWS JAVA ===========\n");
    PhoneObject phoneObject = new PhoneObject(license, dataPath);
    Boolean shouldContinueRunning = true;

    if (!phoneObject.mdPhoneObj.GetInitializeErrorString().equals("No error"))
      shouldContinueRunning = false;

    while (shouldContinueRunning) {
      DataContainer dataContainer = new DataContainer();
      BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

      if (testPhone == null || testPhone.trim().isEmpty()) {
        System.out.println("\nFill in each value to see the Phone Object results");
        System.out.print("Phone:");

        dataContainer.Phone = stdin.readLine();
      } else {
        dataContainer.Phone = testPhone;
      }
      dataContainer.ZipCode = "";

      // Print user input
      System.out.println("\n============================== INPUTS ==============================\n");
      System.out.println("\t               Phone: " + dataContainer.Phone);

      // Execute Phone Object
      phoneObject.ExecuteObjectAndResultCodes(dataContainer);

      // Print output
      System.out.println("\n============================== OUTPUT ==============================\n");
      System.out.println("\n\tPhone Object Information:");

      System.out.println("\t     Area Code: " + phoneObject.mdPhoneObj.GetAreaCode());
      System.out.println("\t        Prefix: " + phoneObject.mdPhoneObj.GetPrefix());
      System.out.println("\t        Suffix: " + phoneObject.mdPhoneObj.GetSuffix());
      System.out.println("\t          City: " + phoneObject.mdPhoneObj.GetCity());
      System.out.println("\t         State: " + phoneObject.mdPhoneObj.GetState());
      System.out.println("\t      Latitude: " + phoneObject.mdPhoneObj.GetLatitude());
      System.out.println("\t     Longitude: " + phoneObject.mdPhoneObj.GetLongitude());
      System.out.println("\t     Time Zone: " + phoneObject.mdPhoneObj.GetTimeZone());
      System.out.println("\t  Result Codes: " + dataContainer.ResultCodes);

      String[] rs = dataContainer.ResultCodes.split(",");
      for (String r : rs) {
        System.out.println("        " + r + ":"
            + phoneObject.mdPhoneObj.GetResultCodeDescription(r, mdPhone.ResultCdDescOpt.ResultCodeDescriptionLong));
      }

      Boolean isValid = false;
      if (testPhone != null && !testPhone.trim().isEmpty()) {
        isValid = true;
        shouldContinueRunning = false;
      }

      while (!isValid) {
        System.out.println("\nTest another phone? (Y/N)");
        String testAnotherResponse = stdin.readLine();

        if (testAnotherResponse != null && !testAnotherResponse.trim().isEmpty()) {
          testAnotherResponse = testAnotherResponse.toLowerCase();
          if (testAnotherResponse.equals("y")) {
            isValid = true;
          } else if (testAnotherResponse.equals("n")) {
            isValid = true;
            shouldContinueRunning = false;
          } else {
            System.out.println("Invalid Response, please respond 'Y' or 'N'");
          }
        }
      }
    }
    System.out.println("\n=============== THANK YOU FOR USING MELISSA JAVA OBJECT ============\n");

  }
}

class PhoneObject {
  // Path to Phone Object data files (.dat, etc)
  String dataFilePath;

  // Create instance of Melissa Phone Object
  mdPhone mdPhoneObj = new mdPhone();

  public PhoneObject(String license, String dataPath) {
    // Set license string and set path to data files (.dat, etc)
    mdPhoneObj.SetLicenseString(license);
    dataFilePath = dataPath;

    // If you see a different date than expected, check your license string and
    // either download the new data files or use the Melissa Updater program to
    // update your data files.
    mdPhone.ProgramStatus pStatus = mdPhoneObj.Initialize(dataPath);

    if (pStatus != mdPhone.ProgramStatus.ErrorNone) {
      // Problem during initialization
      System.out.println("Failed to Initialize Object.");
      System.out.println(pStatus);
      return;
    }

    System.out.println("                DataBase Date: " + mdPhoneObj.GetDatabaseDate());
    System.out.println("              Expiration Date: " + mdPhoneObj.GetLicenseExpirationDate());

    /**
     * This number should match with the file properties of the Melissa Object
     * binary file.
     * If TEST appears with the build number, there may be a license key issue.
     */
    System.out.println("               Object Version: " + mdPhoneObj.GetBuildNumber());
    System.out.println();

  }

  // This will call the lookup function to process the input phone as well as
  // generate the result codes
  public void ExecuteObjectAndResultCodes(DataContainer data) {

    mdPhoneObj.Lookup(data.Phone, data.ZipCode);

    // mdPhoneObj.CorrectAreaCode(data.Phone, data.ZipCode);
    // mdPhoneObj.ComputeDistance(0.0, 0.0, 0.0, 0.0);
    // mdPhoneObj.ComputeBearing(0.0, 0.0, 0.0, 0.0);

    data.ResultCodes = mdPhoneObj.GetResults();

    // ResultsCodes explain any issues Phone Object has with the object.
    // List of result codes for Phone Object
    // https://wiki.melissadata.com/?title=Result_Code_Details#Phone_Object

  }
}

class DataContainer {
  public String Phone;
  public String ZipCode;
  public String ResultCodes;
}
