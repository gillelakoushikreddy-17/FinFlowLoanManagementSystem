$ErrorActionPreference = "Stop"

Write-Host "Generating service-registry..."
curl.exe -f -s -G https://start.spring.io/starter.zip `
    -d dependencies=cloud-eureka-server `
    -d javaVersion=17 `
    -d groupId=com.finflow `
    -d artifactId=service-registry `
    -d name=service-registry `
    -o service-registry.zip
Expand-Archive -Force -Path service-registry.zip -DestinationPath service-registry
Remove-Item service-registry.zip

Write-Host "Generating api-gateway..."
curl.exe -f -s -G https://start.spring.io/starter.zip `
    -d dependencies=cloud-gateway, cloud-eureka `
    -d javaVersion=17 `
    -d groupId=com.finflow `
    -d artifactId=api-gateway `
    -d name=api-gateway `
    -o api-gateway.zip
Expand-Archive -Force -Path api-gateway.zip -DestinationPath api-gateway
Remove-Item api-gateway.zip

Write-Host "Generating auth-service..."
curl.exe -f -s -G https://start.spring.io/starter.zip `
    -d dependencies=web, data-jpa, mysql, security, cloud-eureka, lombok, validation `
    -d javaVersion=17 `
    -d groupId=com.finflow `
    -d artifactId=auth-service `
    -d name=auth-service `
    -o auth-service.zip
Expand-Archive -Force -Path auth-service.zip -DestinationPath auth-service
Remove-Item auth-service.zip

Write-Host "Generating application-service..."
curl.exe -f -s -G https://start.spring.io/starter.zip `
    -d dependencies=web, data-jpa, mysql, cloud-eureka, lombok, validation `
    -d javaVersion=17 `
    -d groupId=com.finflow `
    -d artifactId=application-service `
    -d name=application-service `
    -o application-service.zip
Expand-Archive -Force -Path application-service.zip -DestinationPath application-service
Remove-Item application-service.zip

Write-Host "Generating document-service..."
curl.exe -f -s -G https://start.spring.io/starter.zip `
    -d dependencies=web, data-jpa, mysql, cloud-eureka, lombok, validation `
    -d javaVersion=17 `
    -d groupId=com.finflow `
    -d artifactId=document-service `
    -d name=document-service `
    -o document-service.zip
Expand-Archive -Force -Path document-service.zip -DestinationPath document-service
Remove-Item document-service.zip

Write-Host "Generating admin-service..."
curl.exe -f -s -G https://start.spring.io/starter.zip `
    -d dependencies=web, data-jpa, mysql, cloud-eureka, lombok, validation `
    -d javaVersion=17 `
    -d groupId=com.finflow `
    -d artifactId=admin-service `
    -d name=admin-service `
    -o admin-service.zip
Expand-Archive -Force -Path admin-service.zip -DestinationPath admin-service
Remove-Item admin-service.zip

Write-Host "All backend services generated successfully."
