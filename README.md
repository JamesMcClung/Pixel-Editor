# Pixel Editor

## Changelog

### 0.7.1

#### Additions
- Pass an image path as a command-line argument to edit that file

#### Fixes
- Closing and reopening the app no longer resets which color palette was in use

### 0.7.0

#### Additions
- Color reducer in the "edit" tab

### 0.6.1

#### Additions
- Coordinates of pixel now shown on bottom left
- Added hotkeys for undo and redo

#### Changes
- Smooth tool applies once per pixel
- Warp and smooth tool minimum strength is now 1

#### Fixes
- Smooth no longer favors darker shades
- Eyedropper sets all future alphas, not just the next one

### 0.6.0

#### Additions
- Cut, copy, paste, and delete selections
- Rotate selection in quarter turns
- Reflect selection
- Export sprite, selection, or spritesheet
- Export as animated gif

#### Fixes
- Crash when selecting tool while no layer is present
- Stack overflow when using color select on too big a region
- Marker did not respect alpha
- Could not save images as jpg/jpeg

### 0.5.1

#### Additions
- Eraser respects tool strength
- Press "enter" to drop selection

#### Changes
- Warp tool does not rewarp pixels from same stroke
- Pencil can mix with colors below if alpha is low

#### Fixes
- Switching tools with hotkeys no longer causes visual bugs
- Fixed marker
- Eyedropper did not work properly

### 0.5.0

#### Additions
- Warp tool
- Hue tool
- Smooth tool
- Hotkeys for selecting and resizing tools

#### Changes
- Eyedropper averages color over region, weighted by alpha
- Current tool is highlighted
- Tools show region they will affect

#### Fixes
- Alpha and size were not reset properly when switching tools

### 0.4.0

#### Additions
- Drag tool
- Fill tool
- Color selection tool
- Box selection tool

### 0.3.0

#### Additions
- Cut, copy, and paste
- "Play" button to animate sprite

### 0.2.0

#### Additions
- Eyedropper tool

#### Changes
- Pencil is now default tool

#### Fixes
- Pencil and eraser now respect size
- Drawing out of bounds no longer throws error

### 0.1.0

#### Additions
- Undo and redo
- White background "view" option

#### Fixes
- Bounding frame in spritesheet preview was wrong size
- Background tiling/fill rendered incorrectly
