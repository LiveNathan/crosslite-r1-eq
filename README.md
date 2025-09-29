# CrossLite to R1 EQ Converter

A command-line tool that converts CrossLite EQ settings files (.txt) to d&b audiotechnik R1 format (.rcp).

## What It Does

This tool reads your CrossLite EQ export files and converts them to the R1 format used by d&b audiotechnik's R1 Remote Control software. It supports both single-channel and multi-channel files, automatically detecting the format and creating appropriate output files.

## Download

Download the latest version for your operating system from the [Releases page](https://github.com/yourusername/crosslite-r1-eq/releases):
- **Windows:** `crosslite-r1-eq.exe`
- **Mac (Intel):** `crosslite-r1-converter-macos-intel`
- **Mac (Apple Silicon):** `crosslite-r1-converter-macos-arm`

## Windows Installation & Setup

### Step 1: Download and Place the Executable

1. Download `crosslite-r1-eq.exe` from the releases page
2. Create a folder for the tool (recommended: `C:\Tools\CrossLite-R1`)
3. Move `crosslite-r1-eq.exe` to this folder

### Step 2: Add to Windows PATH (One-Time Setup)

This allows you to run the converter from any folder without typing the full path.

#### Option A: Using System Properties (Easiest)
1. Press `Windows + X` and select "System"
2. Click "Advanced system settings" on the right
3. Click "Environment Variables" button
4. In the bottom section ("System variables"), find and select "Path", then click "Edit"
5. Click "New" and add: `C:\Tools\CrossLite-R1` (or wherever you placed the exe)
6. Click "OK" on all windows
7. **Close and reopen any Command Prompt windows** for changes to take effect

#### Option B: Using Command Line (Advanced)
Open Command Prompt as Administrator and run:
```cmd
setx /M PATH "%PATH%;C:\Tools\CrossLite-R1"
```

### Step 3: Verify Installation

1. Open a **new** Command Prompt (Windows + R, type `cmd`, press Enter)
2. Type: `crosslite-r1-eq help`
3. You should see the help menu

## Basic Usage (Windows)

### Converting a Single File

1. Open Command Prompt (Windows + R, type `cmd`, press Enter)
2. Navigate to your files: `cd C:\Users\YourName\Documents\ShowFiles`
3. List your txt files: `dir *.txt`
4. Convert a file: 
   ```cmd
   crosslite-r1-eq convert-file -i "MyVenue_EQ.txt"
   ```
   This creates `MyVenue_EQ.rcp` in the same folder

### Converting All Files in a Folder

Navigate to your folder and convert all .txt files:
```cmd
cd C:\Users\YourName\Documents\ShowFiles
crosslite-r1-eq convert-directory -i "."
```

### Interactive Mode (Easier for Beginners)

Simply type:
```cmd
crosslite-r1-eq convert-file
```
The program will guide you through selecting files.

## Mac/Linux Installation

### Mac Setup
1. Download the appropriate version for your Mac
2. Open Terminal
3. Make it executable: `chmod +x crosslite-r1-converter-macos-*`
4. Move to Applications: `sudo mv crosslite-r1-converter-macos-* /usr/local/bin/crosslite-r1-eq`

### Linux Setup
Build from source or wait for Linux release (coming soon)

## Examples

### List Available Files
```cmd
crosslite-r1-eq list
```
Shows all .txt files in current directory

### Convert with Custom Output Name
```cmd
crosslite-r1-eq convert-file -i "venue_eq.txt" -o "MainPA_Tuesday.rcp"
```

### Convert to Different Folder
```cmd
crosslite-r1-eq convert-directory -i "." -o "C:\ConvertedFiles"
```

## Multi-Channel Support

The converter automatically detects multi-channel CrossLite files. When a multi-channel file is detected:

- Each channel gets its own .rcp file
- Files are named after the channel (e.g., `ml.rcp`, `c.rcp`, `mr.rcp`)
- Channels without EQ bands are skipped
- All output files are placed in a folder named after the input file

Example:
- Input: `venue_system.txt` (containing ml, c, mr channels)
- Output: `venue_system/ml.rcp`, `venue_system/c.rcp`, `venue_system/mr.rcp`

## Technical Details

### Conversion Limits
The converter automatically adjusts values to fit R1's supported ranges:

| Parameter | R1 Minimum | R1 Maximum | CrossLite Values Outside Range |
|-----------|------------|------------|--------------------------------|
| Gain      | -18 dB     | +12 dB     | Clamped to limits             |
| Q Factor  | 0.1        | 25.0       | Clamped to limits             |
| EQ Bands  | N/A        | 16         | Only first 16 converted       |

### Supported CrossLite Format
The converter reads CrossLite text export files containing lines like:
```
Frequency= 1001.0Hz Gain= -6.0dB Qbp= 0.750
```

## Troubleshooting

### "Command not found" Error
- Make sure you've added the tool to your PATH
- Close and reopen Command Prompt after adding to PATH
- Verify the exe is in the folder you specified

### "File not found" Error
- Make sure you're in the correct directory (`cd` to your files first)
- Use quotes around filenames with spaces: `"My File.txt"`
- Check file extension is `.txt`

### No Output File Created
- Check for error messages in the console
- Verify the input file contains valid EQ data
- Ensure you have write permissions in the output directory

### Windows Defender Warning
Windows may warn about running an unsigned executable. This is normal for command-line tools:
1. Click "More info"
2. Click "Run anyway"
(The tool is safe - you can review the source code on GitHub)

## Command Reference

| Command | Description | Example |
|---------|-------------|---------|
| `list` | Show .txt files in current folder | `crosslite-r1-eq list` |
| `convert-file -i <file>` | Convert single file | `crosslite-r1-eq convert-file -i "eq.txt"` |
| `convert-directory -i <dir>` | Convert all files in folder | `crosslite-r1-eq convert-directory -i "."` |
| `help` | Show detailed help | `crosslite-r1-eq help` |
| `convert-file` | Interactive converter | `crosslite-r1-eq convert-file` |

## Tips for Sound Engineers

1. **Batch Processing**: Keep all your venue files in one folder and convert them all at once with `convert-directory`

2. **File Organization**: Create a folder structure like:
   ```
   ShowFiles/
   ├── CrossLite_Exports/
   │   ├── venue1.txt
   │   └── venue2.txt
   └── R1_Files/
       ├── venue1.rcp
       └── venue2.rcp
   ```

3. **Quick Access**: Create a desktop shortcut to Command Prompt that opens in your show files directory

4. **Verification**: After conversion, you can open the .rcp files in any text editor to verify the XML content

## Support

For issues, feature requests, or questions:
- GitHub Issues: [github.com/yourusername/crosslite-r1-eq/issues](https://github.com/yourusername/crosslite-r1-eq/issues)
- Latest Releases: [github.com/yourusername/crosslite-r1-eq/releases](https://github.com/yourusername/crosslite-r1-eq/releases)

## License

MIT License - see [LICENSE](LICENSE) file for details.

This software is free to use for any purpose. Attribution is required.

---
*Built with Spring Boot and GraalVM Native Image*
