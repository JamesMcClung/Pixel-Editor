# Pixel Editor

## Changelog

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
