# Pixel Editor

## Changelog

### 0.6

#### Features
- cut, copy, paste, and delete selections
- rotate selection in quarter turns
- reflect selection
- export sprite, selection, or spritesheet
- export as animated gif

#### Bug Fixes
- crash when selecting tool while no layer is present
- stack overflow when using color select on too big a region
- marker did not respect alpha
- could not save images as jpg/jpeg

### 0.5.1

#### Features
- erase respects tool strength
- press enter to drop selection

#### Changes
- warp tool does not rewarp pixels from same stroke
- pencil can mix with colors below if alpha is low

#### Bug Fixes
- switching tools with hotkeys no longer causes visual bugs
- fixed marker
- eyedropper did not work properly

### 0.5

#### Features

- warp tool
- hue tool
- smooth tool
- hotkeys to select tools
- arrow keys to resize tools

#### Changes

- eyedropper averages color over region, weighted by alpha
- current tool is highlighted
- tools show region they will affect

#### Bug Fixes

- alpha and size were not reset properly when switching tools

#### Internal Changes

- restructured tools


### 0.4

#### Features

- drag tool to drag sprites and selections
- fill tool
- color selection
- box selection

### 0.3

#### Features

- cut, copy, and paste sprites
- play button

### 0.2

#### Features

- added eyedropper tool
- pencil is now default tool

#### Bug Fixes

- pencil and eraser now respect size
- drawing out of bounds no longer throws error

### 0.1

#### Features

- Undo and redo buttons
- White background for editor

#### Bug Fixes

- bounding frame in spritesheet preview was wrong size
- background tiling/fill rendered incorrectly
