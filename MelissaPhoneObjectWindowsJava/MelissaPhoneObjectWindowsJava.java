import com.melissadata.*;
import java.io.*;

/**
 * Phone Object allows websites and custom applications to verify phone numbers down
 * to 7 and 10 digits, update area codes, and append data about the phone number.
 *
 * <p>High-level flow of this sample:
 * <ol>
 *   <li>SETUP     - create an mdPhone instance, hand it the license string and the
 *                   path to the data files, then Initialize() (one time).</li>
 *   <li>INPUT     - feed a phone number in.</li>
 *   <li>PROCESS   - Lookup() validates the number and appends its data.</li>
 *   <li>READ      - pull the results back out with the Get* getters
 *                   (GetAreaCode, GetCity, GetState, GetTimeZone, ...).</li>
 *   <li>INTERPRET - GetResults() returns comma-separated result codes describing
 *                   what the object did/found; each code has a human description.</li>
 * </ol>
 *
 * <p>The pieces in this file map onto that flow:
 * <ul>
 *   <li>main / RunAsConsole / ParseArguments : console harness (argument parsing + the interactive loop).</li>
 *   <li>PhoneObject                   : thin wrapper around mdPhone that owns setup + the call sequence.</li>
 *   <li>DataContainer                 : plain holder for one record's input and output.</li>
 * </ul>
 *
 * <p>Where mdPhone comes from:
 * The mdPhone and mdPhoneJNI classes in com/melissadata come from mdPhone_JavaCode.zip,
 * which the accompanying MelissaPhoneObjectWindowsJava.ps1 script downloads and
 * expands into com/melissadata on every run. mdPhoneJNI declares the native methods and
 * loads mdPhoneJavaWrapper.dll, the JNI shim that calls into mdPhone.dll.
 *
 * <p>Reference:
 * <ul>
 *   <li>Quickstart    : https://docs.melissa.com/on-premise-api/phone-object/phone-object-quickstart.html</li>
 *   <li>Release notes : https://releasenotes.melissa.com/on-premise-api/phone-object/</li>
 *   <li>Result codes  : https://docs.melissa.com/on-premise-api/phone-object/result-codes.html</li>
 * </ul>
 */
public class MelissaPhoneObjectWindowsJava {

  /**
   * Entry point. Reads the optional command-line arguments, then hands control to
   * RunAsConsole, which performs the actual Phone Object setup and processing.
   *
   * @param args The raw command-line arguments
   * @throws IOException if reading from standard input fails
   */
  public static void main(String args[]) throws IOException {
    // Populated by ParseArguments below.
    String[] arguments = ParseArguments(args);
    String license = arguments[0];
    String testPhone = arguments[1];
    String dataPath = arguments[2];

    RunAsConsole(license, testPhone, dataPath);
  }

  /**
   * Reads the supported command-line options and returns them.
   *
   * <p>Recognized flags (each followed by its value, e.g. "--phone 8002356766"):
   * <ul>
   *   <li>--license / -l   : the Melissa license string</li>
   *   <li>--phone / -p     : a phone number to test in one-shot mode</li>
   *   <li>--dataPath / -d  : path to the Phone Object data files</li>
   * </ul>
   *
   * @param args The raw command-line arguments to parse.
   * @return A String array of { license, testPhone, dataPath }.
   */
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

  /**
   * Sets up the Phone Object once, then drives the input -> process -> output cycle.
   *
   * <p>In interactive mode (no --phone) it loops, asking for a new phone number each pass
   * until the user answers "N". In one-shot mode (--phone supplied) it runs a single
   * pass on testPhone and exits.
   *
   * @param license   The Melissa license string used to initialize the object.
   * @param testPhone A phone number to process in one-shot mode; if empty, the program prompts interactively.
   * @param dataPath  Path to the Phone Object data files.
   * @throws IOException if reading from standard input fails
   */
  public static void RunAsConsole(String license, String testPhone, String dataPath) throws IOException {
    System.out.println("\n\n=========== WELCOME TO MELISSA PHONE OBJECT WINDOWS JAVA ===========\n");

    // Construct the wrapper. This is where the object is licensed, pointed at the
    // data files, and initialized (see the PhoneObject constructor below).
    PhoneObject phoneObject = new PhoneObject(license, dataPath);
    Boolean shouldContinueRunning = true;

    // Gate the program on a successful initialization. If the data files could not
    // be loaded (bad/expired license, missing or wrong-path data files, ...),
    // GetInitializeErrorString() returns the reason instead of "No error" and we
    // skip the processing loop entirely.
    if (!phoneObject.mdPhoneObj.GetInitializeErrorString().equals("No error"))
      shouldContinueRunning = false;

    while (shouldContinueRunning) {
      // Holder for this pass's input and result codes.
      DataContainer dataContainer = new DataContainer();
      BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

      if (testPhone == null || testPhone.trim().isEmpty()) {
        // Interactive mode: prompt the user for a phone number.
        System.out.println("\nFill in each value to see the Phone Object results");
        System.out.print("Phone:");

        dataContainer.Phone = stdin.readLine();
      } else {
        // One-shot mode: use the phone number passed on the command line.
        dataContainer.Phone = testPhone;
      }
      dataContainer.ZipCode = "";

      // Print user input
      System.out.println("\n============================== INPUTS ==============================\n");
      System.out.println("\t                Phone: " + dataContainer.Phone);

      // Execute Phone Object
      // Runs the Lookup and stores the result codes on dataContainer.
      phoneObject.ExecuteObjectAndResultCodes(dataContainer);

      // Print output
      // Each Get* getter below returns one component the object produced for the most
      // recently processed phone number. These read directly from the mdPhone instance,
      // which still holds the results from the Execute call above.
      System.out.println("\n============================== OUTPUT ==============================\n");
      System.out.println("\n\tPhone Object Information:");

      System.out.println("\t        Area Code: " + phoneObject.mdPhoneObj.GetAreaCode());
      System.out.println("\t           Prefix: " + phoneObject.mdPhoneObj.GetPrefix());
      System.out.println("\t           Suffix: " + phoneObject.mdPhoneObj.GetSuffix());
      System.out.println("\t             City: " + phoneObject.mdPhoneObj.GetCity());
      System.out.println("\t            State: " + phoneObject.mdPhoneObj.GetState());
      System.out.println("\t         Latitude: " + phoneObject.mdPhoneObj.GetLatitude());
      System.out.println("\t        Longitude: " + phoneObject.mdPhoneObj.GetLongitude());
      System.out.println("\t        Time Zone: " + phoneObject.mdPhoneObj.GetTimeZone());
      System.out.println("\t     Result Codes: " + dataContainer.ResultCodes);

      // Other data the Phone Object can return - uncomment any you need:
      //System.out.println("\t    New Area Code: " + phoneObject.mdPhoneObj.GetNewAreaCode());
      //System.out.println("\t        Extension: " + phoneObject.mdPhoneObj.GetExtension());
      //System.out.println("\t       CountyFips: " + phoneObject.mdPhoneObj.GetCountyFips());
      //System.out.println("\t       CountyName: " + phoneObject.mdPhoneObj.GetCountyName());
      //System.out.println("\t              Msa: " + phoneObject.mdPhoneObj.GetMsa());
      //System.out.println("\t             Pmsa: " + phoneObject.mdPhoneObj.GetPmsa());
      //System.out.println("\t   Time Zone Code: " + phoneObject.mdPhoneObj.GetTimeZoneCode());
      //System.out.println("\t     Country Code: " + phoneObject.mdPhoneObj.GetCountryCode());
      //System.out.println("\t         Distance: " + phoneObject.mdPhoneObj.GetDistance());

      // Result codes come back as a single comma-separated string (e.g. "PS01,PS08").
      // Split it and ask the object for a readable description of each code.
      // ResultCodeDescriptionLong requests the long-form text; a short form is also
      // available via ResultCodeDescriptionShort
      String[] rs = dataContainer.ResultCodes.split(",");
      for (String r : rs) {
        System.out.println("           " + r + ":"
            + phoneObject.mdPhoneObj.GetResultCodeDescription(r, mdPhone.ResultCdDescOpt.ResultCodeDescriptionLong));
      }

      Boolean isValid = false;

      // In one-shot mode there is nothing more to do after a single pass: mark the
      // input handled and stop the outer loop.
      if (testPhone != null && !testPhone.trim().isEmpty()) {
        isValid = true;
        shouldContinueRunning = false;
      }

      // Interactive mode: ask whether to process another phone number. Keep prompting
      // until we get a valid Y/N. "N" ends the program; "Y" falls through to another pass.
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

/**
 * Wrapper that owns a single Melissa Phone Object instance and encapsulates the two
 * things every Melissa object needs: one-time setup (license + data files) and the
 * per-record processing sequence. Reuse one instance across many numbers; do NOT
 * re-initialize per number.
 */
class PhoneObject {
  // Path to the Phone Object data files.
  String dataFilePath;

  // The underlying Melissa Phone Object instance.
  mdPhone mdPhoneObj = new mdPhone();

  /**
   * Performs the mandatory one-time setup, in this required order:
   * <ol>
   *   <li>SetLicenseString - authorize the object.</li>
   *   <li>Initialize       - point the object at the data files and load them.</li>
   * </ol>
   *
   * @param license  The Melissa license string used to authorize the object.
   * @param dataPath Path to the folder containing the Phone Object data files.
   */
  public PhoneObject(String license, String dataPath) {
    // Set license string and set path to data files
    mdPhoneObj.SetLicenseString(license);
    dataFilePath = dataPath;

    // Point the object at the data files and load them. The returned ProgramStatus
    // reports whether initialization succeeded.
    // If you see a different date than expected, check your license string and either download the new data files
    // or use the Melissa Updater program to update your data files.
    mdPhone.ProgramStatus pStatus = mdPhoneObj.Initialize(dataPath);

    // If an issue occurred, please investigate the common causes.
    // Common causes: an invalid/expired license, or missing/wrong-path data files.
    if (pStatus != mdPhone.ProgramStatus.ErrorNone) {
      System.out.println("Failed to Initialize Object.");
      System.out.println(pStatus);
      return;
    }

    // Diagnostic information, handy for confirming the object loaded the data you expect:

    // Build date of the data files
    System.out.println("                DataBase Date: " + mdPhoneObj.GetDatabaseDate());

    // When the license stops working
    System.out.println("              Expiration Date: " + mdPhoneObj.GetLicenseExpirationDate());

    // This number should match with the file properties of the Melissa Object binary file.
    // If TEST appears with the build number, there may be a license key issue.
    System.out.println("               Object Version: " + mdPhoneObj.GetBuildNumber());
    System.out.println();

  }

  /**
   * Runs the full Phone Object processing sequence for one phone number and captures
   * its result codes. This is the canonical per-record call pattern to copy into your
   * own application:
   * Lookup -> GetResults
   *
   * @param data The record to process. Its Phone is read as input, and ResultCodes is
   *             populated with this run's result codes.
   */
  public void ExecuteObjectAndResultCodes(DataContainer data) {

    // Validate the number and append its data
    mdPhoneObj.Lookup(data.Phone, data.ZipCode);

    // Other Phone Object operations, available if you need them:
    // mdPhoneObj.CorrectAreaCode(data.Phone, data.ZipCode);
    // mdPhoneObj.ComputeDistance(0.0, 0.0, 0.0, 0.0);
    // mdPhoneObj.ComputeBearing(0.0, 0.0, 0.0, 0.0);

    // Collect the result codes for this run
    // ResultsCodes explain any issues Phone Object has with the object.
    // List of result codes for Phone Object
    // https://docs.melissa.com/on-premise-api/phone-object/result-codes.html
    data.ResultCodes = mdPhoneObj.GetResults();
  }
}

/**
 * Data holder for a single record: carries the input phone number in and the result
 * codes out.
 */
class DataContainer {
  // Input: the phone number to process.
  public String Phone;

  // Input: optional ZIP code passed to Lookup to help disambiguate the number.
  // This sample always sets it to an empty string, so Lookup receives an empty string.
  public String ZipCode;

  // Output: comma-separated result codes from GetResults().
  public String ResultCodes;
}
