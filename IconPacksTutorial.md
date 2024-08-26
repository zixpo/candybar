# Icon Pack Manager

## How does it work?

We have an icon pack (color) in our main app, and per additional icon pack (color) we want to add we have to create an additional icon pack (color) app. These apps only contain the icons and have no interface, they only show a dialog with a button that points back to the main app.

Every launcher points to an app and uses those drawables to apply the icon pack.

In our main app we can choose which icon pack we want to apply and send it to the launcher. 

All app icons will be shown in the main app and we can apply single icons through the launcher, open the app and choose an icon.

We can add multiple colors or variations per icon pack. For example we have an icon pack called "Candybar" and have 3 different colors: blue, red, green. 


# Main App Configurations

### Enable settings in main app

1. Open dashboard_configurations.xml
2. Set enable_icon_packs to true

    ```java
    <bool name="enable_icon_packs">true</bool>
    ```

3. Choose a name for your default icon pack which will be included in our main app.

    > !!! Do not use dashes and double spaces here. If icon pack name exists of multiple words, use spaces.

    ```java
    <string name="icon_pack">candybar</string>
    ```

4. Choose a name for your default icon pack, or give it a different name of your choosing

    > !!! Do not use dashes and double spaces here. If color exists of multiple words, use spaces.

    ```java
    <string name="icon_pack_color">light blue</string>
    ```


### Choose an icon for your icon pack and color to be displayed in the app

1. Replace `ic_icon_pack.png` in the drawable-nodpi-v5 folder with your own icon.
    > This icon represents the icon pack image shown in the "Icon Packs" list in the app.
2. Replace `ic_icon_pack_color.png` in the drawable-nodpi-v5 folder with your own icon.
    > This icon represents the icon pack color image shown in the "Icon Packs" dialog in the app.


## Icon Pack (color) App

### One Time Configuration

1. Copy the folder `Candybar_Icon_Pack` and paste it wherever you want, rename the folder to your name of choice 
2. Open `configuration.xml` which is in the values folder.
   1. Change the string value with name `app_name` to the name of your choice. 
      ```java
      <string name="app_name">New Icon Pack Name</string>
      ```
   2. Change the string value with name `icon_pack` to the name of your choice.
      > If you want to add a different color to an already existing icon pack, make sure this name is the same as the others. In this example we used candybar in our main app already, so this will become a different color for the same icon pack.
      
      > !!! Do not use caps here. If icon pack name exists of multiple words, use spaces.
      ```java
      <string name="icon_pack">candybar</string>
      ```
   3. Change the string value with the name `icon_pack_color` to the name of your choice.
      > !!! Do not use caps here. If color exists of multiple words, use space.
      ```java
      <string name="icon_pack_color">navy blue</string>
      ```
3. Open the folder in Android Studio, click on `Edit` at the top navigation, then go to `Find` and click on `Replace in Files`.
   1. In the first text box type in `com.candybar.dev.candybar_new_color`, in the second text box type in the following structure:
   `main_app_identifier + . + icon_pack + _color`. Then click on `Replace All`. It is very important to follow this structure or it will not work.

      **Example:**
   - Main app package identifier: `com.candybar.dev`
   - Icon Pack Name: `candybar`
        > *This needs to match what's inside:
        > ```java
        > <string name="icon_pack">candybar</string>
        > ```
   - Color: `navy blue`
      > *This needs to match what's inside:
      > ```java
      > <string name="icon_pack_color">navy blue</string>
      > ```
      > Convert your spaces into underscores
    - Output example:
      ```java
      com.candybar.dev.candybar_navy_blue
      ```
   2. In the first text box type in `com.candybar.dev`, in the second text box type in your main app's package identifier. Then click on `Replace All`.
   3. Change the name of the folders to your chosen identifier:
        1. Find package directory `com.candybar`, right click on the directory, `Refractor > Rename` and click on `All Directories`, fill in your identifier which comes after the `com.` and click `Refractor`.
            > This will only change `candybar`. If you want to change the `com` you'll have to do it manually outside Android Studio. Make sure you do this for all 3 folders you find under `java` in Android studio. To find where these folders are, just right click them in android studio and click `Open In > Explorer`.
        2. Find package directory `dev` right under the one we just changed. Right click on the directory, `Refractor > Rename` and click on `All Directories`, fill in your identifier which comes after the `com.candybar.` (this is the name you have chosen of your main app identifier sub domain) and click `Refractor`. 
5. Replace `ic_icon_pack.png` in the drawable folder with your own icon.
    > This icon represents the icon pack image shown in the "Icon Packs" list in the app.

    > Note: The `ic_icon_pack.png` from the main app will be used over the one from the side app.
6. Replace `ic_icon_pack_color.png` in the drawable folder with your own icon.
    > This icon represents the icon pack color image shown in the "Icon Packs" dialog in the app.
7. Replace the `ic_launcher.png` in the following folders to change your app icon.
    - res/mipmap-mdpi/ic_launcher.png
    - res/mipmap-hdpi/ic_launcher.png
    - res/mipmap-xhdpi/ic_launcher.png
    - res/mipmap-xxhdpi/ic_launcher.png
    - res/mipmap-xxxhdpi/ic_launcher.png

### Add your icons

This follows the same structure as the main app.

1. Put your icons in the `res/drawable` folder.
2. Update your `drawable.xml` file in the `res/xml` folder.
3. Update your `appfilter.xml` file in the `res/xml` folder.

When you are done, click on `Sync Project with Gradle Files` before running or exporting the app.
