# HTTP Client Plus (IntelliJ Plugin)

A comprehensive Postman-like tool window for IntelliJ IDEA that lets you compose, organize, and execute HTTP requests. All requests are saved as standard IntelliJ HTTP Client (.http) files, making them portable, shareable, and version-controlled.

[![JetBrains Marketplace](https://img.shields.io/jetbrains/plugin/v/28340-http-client-plus)](https://plugins.jetbrains.com/plugin/28340-http-client-plus)

## 📦 Installation

### From JetBrains Marketplace
<a href="https://plugins.jetbrains.com/plugin/28340-http-client-plus" target="_blank">
    <img src="https://raw.githubusercontent.com/MaheshBabu11/http-client-plus/edad1fa78a729d8f54405d10d38c2572f82aa9f9/img/installation_button.svg" height="52" alt="Get from Marketplace" title="Get from Marketplace">
</a>

### Manual Installation
1. Open IntelliJ IDEA
2. Go to `File → Settings → Plugins`
3. Search for "HTTP Client Plus"
4. Click Install

## 🚀 Features

### Core Request Building
- **Request Builder**: Method dropdown (GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS), URL field with inline Send button
- **Request Organization**: Name field used as .http filename and request label, collection-based organization
- **URL Parameters**: Visual key-value editor that automatically appends to URL
- **HTTP Headers**: Key-value table for custom headers
- **Request Body**: Multiple content types with JSON beautification support
- **Multipart Forms**: Complete form-data builder with boundary control, text/file parts, and per-part content types

### Authorization Support
- **Multiple Auth Types**: None, Basic, Bearer Token, Digest, Custom Header
- **Basic Auth**: Username/password with automatic Base64 encoding
- **Bearer Token**: Simple token input for OAuth/JWT
- **Digest Auth**: Username/password for digest authentication
- **Custom Headers**: Flexible authorization header configuration

### Environment Management
- **Environment Variables**: Define and use variables across requests
- **Public/Private Environments**: Separate files for shared and sensitive data
- **Environment Files**: `http-client.env.json` (public) and `http-client.private.env.json` (private)
- **Variable Substitution**: Use `{{variable}}` syntax in requests

### Import & Export
- **Postman Import**: Import collections from Postman JSON files
- **Drag & Drop**: Drop Postman collection files directly into the tool
- **Collection Organization**: Automatic conversion to HTTP Client Plus format

### Response Management
- **Response Viewing**: Built-in response viewer with syntax highlighting
- **Response Saving**: Automatic response file storage with timestamps
- **Response History**: Browse previous responses for each request
- **Response Handler Scripts**: Add custom JavaScript for response processing

### Development Integration
- **REST Controller Integration**: Gutter icons in Spring REST controllers for quick request creation
- **Automatic Request Detection**: Parse Spring mapping annotations (@GetMapping, @PostMapping, etc.)
- **IDE Integration**: Seamless integration with IntelliJ's HTTP Client

### File Management
- **Saved Requests**: Browse, search, and manage all saved requests
- **Collection Organization**: Group requests into logical collections
- **File Operations**: Rename, delete, duplicate requests
- **Search & Filter**: Find requests by name, method, or collection
- **Drag & Drop**: Reorganize requests between collections

### Advanced Features
- **Request Validation**: Automatic validation and error checking
- **Content-Type Detection**: Smart content-type headers for JSON requests
- **File Upload Support**: Complete multipart file upload functionality
- **Settings Configuration**: Customizable storage locations and behavior
- **Keyboard Shortcuts**: Full keyboard navigation support

## 📋 Requirements

- IntelliJ IDEA 2024.2+ (Ultimate Edition)
- Built-in HTTP Client enabled (default)
- JDK 17+ for building from source

## 🎥 Demo Video

[![HTTP Client Plus Demo](https://img.youtube.com/vi/q7APwIfE7Og/hqdefault.jpg)](https://www.youtube.com/watch?v=q7APwIfE7Og)

## 🛠️ Build and Run

```bash
# Build the plugin
./gradlew build

# Run in development IDE
./gradlew runIde
```

## 🎯 Multi-Version Support

Build for specific IntelliJ versions using the `ideLine` property:

```bash
# Default 2025.2 baseline
./gradlew clean build -x test

# 2025.1
./gradlew clean build -PideLine=251 -x test

# 2024.3
./gradlew clean build -PideLine=243 -x test

# 2024.2
./gradlew clean build -PideLine=242 -x test
```

Each build creates a versioned ZIP (e.g., `http-client-plus-1.0.5-243.zip`) in both `build/distributions/` and `dist/1.0.5/`.

### Plugin Verification
```bash
./gradlew runPluginVerifier -PideLine=243
```

## 📖 Usage Guide

### Getting Started
1. **Open Tool Window**: View → Tool Windows → HTTP Client Plus
2. **Create Collection**: Click "Add New" to create a request collection
3. **Build Request**: Enter name, select method, add URL
4. **Configure Details**: Add parameters, headers, body, or authorization
5. **Send Request**: Click "Send" to save as .http file
6. **Execute**: Run manually from editor (Ctrl+Enter)

### Working with Collections
- **Create**: Use "Add New" button to create collections
- **Organize**: Group related requests together
- **Browse**: Use the Saved Requests tab to navigate
- **Search**: Filter requests by name or collection

### Environment Variables
1. **Setup**: Use the Environment tab to define variables
2. **Public Variables**: Stored in `http-client.env.json`
3. **Private Variables**: Stored in `http-client.private.env.json`
4. **Usage**: Reference with `{{variable_name}}` syntax

### Importing from Postman
1. **File Import**: Use File menu or drag & drop
2. **Collection Conversion**: Automatic conversion to .http format
3. **Preserve Structure**: Maintains folder organization
4. **Environment Mapping**: Converts Postman variables

### Authorization Setup
1. **Select Type**: Choose from dropdown (Basic, Bearer, etc.)
2. **Configure**: Enter credentials or tokens
3. **Apply**: Headers automatically added to requests
4. **Environment**: Use variables for sensitive data

## 📁 File Organization

```
<project>/
├── http-client-plus/
│   ├── collections/           # Request collections
│   │   ├── Collection_1/     # Individual collection
│   │   │   ├── request1.http # HTTP request files
│   │   │   └── request2.http
│   │   └── Collection_2/
│   └── environments/         # Environment files
│       ├── http-client.env.json         # Public variables
│       └── http-client.private.env.json # Private variables
```

## ⚙️ Configuration

### Storage Location
- **Default**: `<project>/http-client-plus/collections`
- **Custom**: Configure in plugin settings
- **Environment**: `<project>/http-client-plus/environments`

### Request Files
- **Format**: Standard IntelliJ HTTP Client format
- **Execution**: Manual via Ctrl+Enter in editor
- **Sharing**: Version control friendly

## 🔧 Development Features

### REST Controller Integration
- **Gutter Icons**: Clickable icons in Spring controllers
- **Auto-Detection**: Recognizes mapping annotations
- **Quick Creation**: Generate requests from controller methods
- **Parameter Mapping**: Extract path variables and parameters

### Response Processing
- **Handler Scripts**: Add JavaScript for response processing
- **Global Variables**: Set variables from response data
- **Assertions**: Add custom validation logic
- **Chaining**: Use response data in subsequent requests

## 📝 Notes

- All requests use standard IntelliJ HTTP Client format
- Files can be edited manually in the IDE
- Supports all HTTP Client features (environments, pre-request scripts, etc.)
- Perfect for API testing, development, and documentation
- Team collaboration through version control

## 🤝 Contributing

This is an open-source project. Contributions, bug reports, and feature requests are welcome!
