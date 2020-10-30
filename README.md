# EHRBase Aql-Editor Backend  

## Installation

### Build
```bash
mvn clean install
```

## Usage
- Swagger: [Swagger UI](http://localhost:8090/aqleditor/swagger-ui/)
- Postman: import `/postman/aqleditor.postman_collection.json`
## Open Point
- storage options 
    - filesystem
    - extern openEHR platform
- multi language support
- build better aql
    - set as correctly
    - remove unnecessary brackets
- aql validation and meaningful error messages
- add support to where:
    - not
    - exist
    - matches
    - terminology

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
[APACHE 2.0](https://www.apache.org/licenses/LICENSE-2.0)
