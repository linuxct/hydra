# Hydra

It is a tool that allows a user to experience and test the Play Integrity Remediation feature, first presented by Google at 2024's I/O.
Please note: Due to the need for Google Cloud credentials, redistributable/prebuilt versions of Hydra will not be facilitated. You will have to build it yourself. 

## Build

You can use Android Studio to build the application, or you can build it by using the CLI.  

Navigate to the folder where the source code is located:  
```cd /path/where/you/downloaded/Hydra/```  

Now, login to Google Cloud using a Google account of your choice. While doing so, create a Google Cloud project for Hydra.
After you have selected the project, go to the API library and enable the Play Integrity API there.
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
TODO

