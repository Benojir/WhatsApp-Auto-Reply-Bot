name: Build APK

on:
  push:
    branches:
      - main  # Change this to your default branch if needed
  pull_request:
    branches:
      - main  # Change this to your default branch if needed

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v3

    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Download and Set Up Android SDK
      run: |
        sudo apt-get update
        sudo apt-get install -y wget unzip
        mkdir -p $HOME/android-sdk
        wget https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip -O cmdline-tools.zip
        unzip cmdline-tools.zip -d $HOME/android-sdk/cmdline-tools
        yes | $HOME/android-sdk/cmdline-tools/bin/sdkmanager --licenses
        $HOME/android-sdk/cmdline-tools/bin/sdkmanager "platform-tools" "platforms;android-30" "build-tools;30.0.3"

    - name: Build APK
      run: ./gradlew assembleRelease

    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: my-app-release
        path: app/build/outputs/apk/release/app-release.apk
