<p align="center" width="100%">
  <img src="https://github.com/linuxct/hydra/blob/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp?raw=true" alt="logo"></img><br/>
</p>

# Hydra

Tool that allows a user to experience and test the Play Integrity Remediation feature, [first presented by Google at 2024's I/O](https://io.google/2024/explore/f757438a-844f-4c59-8dd4-9a5580a5e23d/).  

**Please note**: Due to the need for Google Cloud credentials, redistributable/prebuilt versions of Hydra will not be facilitated. 
**You will have to build it yourself.**

## Build

You can use Android Studio to build the application, or you can build it by using the CLI.  

Navigate to the folder where the source code is located:  
```cd /path/where/you/downloaded/Hydra/```  

Now, login to Google Cloud using a Google account of your choice. While doing so, create a Google Cloud project for Hydra.  
After you have selected the project, go to the API library and enable the Play Integrity API there.  
Copy the Google Cloud project ID and replace the one shown in [line 46 of the PlayIntegrityService.kt file](https://github.com/linuxct/hydra/blob/main/app/src/main/kotlin/space/linuxct/hydra/integrity/PlayIntegrityService.kt#L46) [TODO: Make this parametrizable via Gradle].  
Lastly, create a Google Cloud Service account under this project. You can name it anything you want.   
After you do that go to the Keys tab, create a new key and download the JSON file.  
Place this JSON at the path (renaming it where needed): `app/src/main/res/raw/hydra_service_account.json`  

Now, check that Gradle runs properly by executing:  
For Linux/MacOS: `./gradlew tasks`  
For Windows: `gradlew tasks`  

You can now build the application in release or debug flavor:   
`./gradlew assemble`  

After it's done building, you will now need to sign the resulting APK by using apksigner, or jarsigner. Here's an example:  
```apksigner sign --ks /path/to/example.keystore --ks-pass pass:"EXAMPLEPASSWORD" --v1-signing-enabled true --v2-signing-enabled true --verity-enabled true *.apk```

## Usage
Upon building and installing the app, you will be presented with a single button.  
Upon pressing it, Play Integrity will run on the device, and its result will be decrypted through Google's Cloud API.  
The result will be presented in the form of a dialog, with a 'Continue' button at the end of it.  
Upon pressing the Continue button, you will be prompted to choose between the (as of writing this) 3 possible [types of remediations](https://developer.android.com/google/play/integrity/remediation):

- [GET_LICENSED](https://developer.android.com/google/play/integrity/remediation#get-licensed-dialog) will request you to install the app from Play Store, before returning control to the developer's app. This remediation can be requested by the app developer regardless of whether you installed the app from Play Store or not. App developers can request you  when you installed the APK version and want to switch to the Play Store version.
  
- [CLOSE_UNKNOWN_ACCESS_RISK](https://developer.android.com/google/play/integrity/remediation#close-unknown-access-risk-dialog) will make any unknown, considered-to-be-risky-by-Google-Play apps to stop executing before returning control to the developer's app. This includes any app that was not reviewed by Google upon submission to Play Store, and which can show screen overlays, perform screen recording, etc. The exact rules used to determined which apps pose a risk are not fully known, but we know [based on Google's own descriptions](https://github.com/googleapis/google-api-dotnet-client/blob/main/DiscoveryJson/playintegrity.v1.json#L250-L257) that they detect "apps that could be used to read or capture the requesting app (such as a screen recording app), display overlays over the requesting app, or control the device (such as a remote support app)". For these, Google Play will show a dialog saying which apps need to be closed (based on their criteria) and a button to close them. This can only be triggered when a risky app is open and running. You can tell if this is the case by reading the Play Integrity's result JSON, under `environmentDetails::appAccessRiskVerdict`.
  
- [CLOSE_ALL_ACCESS_RISK](https://developer.android.com/google/play/integrity/remediation#close-all-access-risk-dialog) will do the same as CLOSE_UNKNOWN_ACCESS_RISK, but with the exception that this will also prevent apps known by Play Store, or which come preloaded with the device's firmware, from executing, and will request them to be closed. The criteria used to detect which apps fall under this criteria is the same as for CLOSE_UNKNOWN_ACCESS_RISK.

After you run any of the 3 types of remediation, you will get [Toasts with the result codes](https://developer.android.com/google/play/integrity/reference/com/google/android/play/core/integrity/model/IntegrityDialogResponseCode). Depending on these result codes, the app developer could allow you to continue executing the app or not. Each result code has a meaning:
- DIALOG_CANCELLED: The user was shown the Integrity Dialog, but did not interact with it.
- DIALOG_FAILED: An error occurred when trying to show the Integrity Dialog.
- DIALOG_SUCCESSFUL: The user was shown the Integrity Dialog, and successfully interacted with it.
- DIALOG_UNAVAILABLE: The Integrity Dialog is unavailable.

## Screenshots
<p align="center" width="100%">
  <img width="30%" src="https://github.com/linuxct/hydra/blob/main/assets/1.png" alt="Application"></img><br/>
  First screen shown to the user upon install
</p>
<br/>

<p align="center" width="100%">
  <img width="30%" src="https://github.com/linuxct/hydra/blob/main/assets/2.png" alt="Play Integrity verdict"></img>&emsp;<img width="30%" src="https://github.com/linuxct/hydra/blob/main/assets/6.png" alt="Play Integrity verdict"></img><br/>
  Examples of possible Play Integrity verdict JSONs. <br/>
  The left one comes from an app which was signed using a different key than the one known by Google Play. <br/>
  The one on the right comes from a device which has apps that are considered to be risky by Google Play.
</p>
<br/>

<p align="center" width="100%">
  <img width="30%" src="https://github.com/linuxct/hydra/blob/main/assets/3.png" alt="Dialog with Play Integrity remediations"></img><br/>
  Dialog shown after hitting 'Continue' on the Play Integrity verdict dialog.<br/>
  In it, one of the 3 possible remediations can be selected.
</p>
<br/>

<p align="center" width="100%">
  <img width="30%" src="https://github.com/linuxct/hydra/blob/main/assets/4.png" alt="Google Play requesting the app to be reinstalled"></img>&emsp;<img width="30%" src="https://github.com/linuxct/hydra/blob/main/assets/5.png" alt="Google Play requesting the app to be reinstalled"></img>&emsp;<img width="30%" src="https://github.com/linuxct/hydra/blob/main/assets/7.png" alt="Google Play requesting the user to close the screen recorder app"></img><br/>
  Examples of the possible remediation behaviours. <br/>
  The first two images show how Google Play requests a user to reinstall an app from Play Store, removing the previous version. <br/>
  The last image shows how Google Play requests a user to stop the screen recorder app before proceeding to the app.
</p>
<br/>

<p align="center" width="100%">
  <img width="30%" src="https://github.com/linuxct/hydra/blob/main/assets/8.png" alt="Example of Play Integrity remediation result"></img><br/>
  Example of a Play Integrity remediation result, sent back to the app after the control is returned to the developer's app.
</p>
<br/>
