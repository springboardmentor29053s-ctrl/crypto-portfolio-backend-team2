Crypto Portfolio Tracker - Backend

📌 Project Overview
This is the backend of the Crypto Portfolio Tracker with Risk & Scam Analysis.  
It handles authentication, exchange integration, holdings, trades, pricing, and risk-related features.


🚀 Features
- User Signup and Login using JWT Authentication  
- Secure password encryption using BCrypt  
- Exchange connection support (Binance Testnet)  
- Holdings management  
- Trade management (Buy/Sell)  
- Portfolio summary and P&L calculation  
- Live crypto price integration  
- Scheduling support for periodic tasks  


🛠 Tech Stack
- Spring Boot  
- Java  
- MySQL  
- Spring Security  
- JWT  
- JPA / Hibernate  


⚙️ How to Run the Project

1. Configure Database
Update `application.properties` with your local MySQL credentials.

2. Run the Backend

Using Maven:
mvn spring-boot:run

OR

Using Maven Wrapper:
./mvnw spring-boot:run

For Windows:
mvnw.cmd spring-boot:run


🌐 Server
Backend runs at:
http://localhost:8080


📊 Main Modules
- Authentication  
- Exchange Connection  
- Holdings  
- Trades  
- Dashboard  
- Pricing  
- Risk Detection  


⚠️ Notes
- `application.properties` is excluded from Git for security  
- Binance Testnet is used for exchange integration  
