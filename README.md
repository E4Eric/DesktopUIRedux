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

The layout logic is aided by the observation that IDEs layouts always tile completely (they don't have 'holes'). I take the approach that each top-level step in the layout chews some 'slab' from the existing available space and reduces the remaining space accordingly. So basically it chews around the edges leaving the central area open (to show the editor area).

Styling is done through defined 'GuiAssets'. This is a directory that contains the following structure:
 - GuiAssets / Skins / Individual Skins (eg. 'Eclipse')
   - Frames (which contains an image and style parameter file for each 'type' of element)
   - Icons (which contains the icons to use for various common operations like 'Save'....)

The actual rendering is done using the usual 'layout' / 'paint' pattern. The difference here is that the layout
step is just a simple rectangle manager which sets each element's *screen* position, alllowing paint step to just recursively paint each element with no further processing.

## Behavior

Behavior in all UI's is a process of mapping the avaiable inputs from keyboard / mouse / other? to response 'actions'. Here the UI uses the same action implementation as the User uses to define their own. All actions are simple POJO's (found in the "Actions" directory)

## Use

To use this example:
 - Clone this repo into an eclipse project
 - Change the project to reference the provided SWT jar
 - Change the project to reference a JSON implementation (not provided)
 - Run "UIModelTest"

You can switch skins by using 'e' (Eclipse), 'E' (Eclipse Dark), 't' (Test) and for fun 'b' (Brass, uses Baroque frames...;-)
Entering 's' is a performance test that loops the layout / draw cycle for 1 second and reports the count

# TBD

I left these as I was anticipating moving this POC into one containing the ModelRedux (a minimalist model engine) since it would properly support reacting to changes in the UI model. However even as small as the ModelRedux is I'm considering whether it's necessary at all.

 - Add a 'view' layout (just a 'pane' layout with a 'resizable' flag and adds a 'drag rect' to the side opposite of the one that the element is on)
 - Hook up more of the events, specifically sash resizing and hover to open sub-menus
 - KB navigation both for the UI generally and menus in particular

