# UIRedux

The goal of this project is to demonstrate a minimalist approach to implementing an IDE container.

THe current implementation uses the File System, Java and SWT but minimizes its use of each. 
 - It reads and checks file's time stamps
 - It creates a SWT Shell for its container and uses the graphics context to
   - Compute the rectangle for a given text / font
   - Draw the text
   - Copy areas from an image onto the screen

## Approach

This approach is based on a realization that most of what we think of as UI elements (Menus, Toolbars...) are illusions; they're a story we tell the User so they know how to use the UI. As long as the appearance on the screen and how it reacts is appropriate to the User's expectations then any implementation is OK.

Given this let's look at both what the User see and how the UI reacts to user input...

## Rendering

What do we need to render an accurate representation of existing IDEs ?

Turns out that there are only two concepts needed:
 - Containers
   - Used to control layout
   - Has a list of child elements
 - Labels
   - Has label text and / or an icon
   - Size determined based on the text and / or icon's bounding box

Styling is done through defined 'GuiAssets'. This is a directory that contains the following structure:
 - GuiAssets / Skins / Individual Skins (eg. 'Eclipse')
   - Frames (which contains an image and style parameter file for each 'type' of element)
   - Icons (which contains the icons to use for various common operations like 'Save'....)

The actual rendering is done using the usual 'layout' / 'paint' pattern. The difference here is that the layout
step is just a simple rectangle manager which sets each element's *screen* position, alllowing paint step to just recursively paint each element with no further processing.

## Behavior

Behavior in all UI's is a process of mapping the avaiable inputs from keyboard / mouse / other? to response 'actions'. Here the UI uses the same action implementation as the User uses to define their own. All actions are simple POJO's (found in the "Actions" directory)

# Use

To use this example:
 - Clone this repo into an eclipse project
 - Change the project to reference the provided SWT jar
 - Change the project to reference a JSON implementation (not provided)
 - Run "UIModelTest"


