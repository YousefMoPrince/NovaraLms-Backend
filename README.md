🚀 NovaraLms
NovaraLms is a robust Learning Management System (LMS) backend designed for high-performance content delivery and secure educational management. It focuses on optimized video streaming and scalable data architecture.

🏗️ Architecture & Design
The system utilizes a Hybrid Database Architecture to balance data integrity with high availability:

PostgreSQL: Handles relational data requiring strict ACID compliance, such as User Identity, Student Enrollments, and Course structures.

MongoDB: Manages unstructured or high-volume data, including Video Metadata and System Logs, ensuring horizontal scalability.

🛠️ Tech Stack
Framework: Spring Boot 3 (Java 21)

Security: Spring Security & JWT (Stateless Authentication)

Persistence: Spring Data JPA & Spring Data MongoDB

Streaming: Byte-Range Requests using ResourceRegion to support video seeking (scrubbing) and efficient bandwidth usage.

✨ Key Features
Secure Video Streaming: Implements advanced streaming logic that prevents direct file access. Content is served in chunks, ensuring fast load times and content protection.

Role-Based Access Control (RBAC): Fine-grained authorization for Admins, Instructors, and Students.

Automated Course Management: Dedicated endpoints for instructors to manage video uploads, dynamic thumbnail generation, and course metadata.

Performance Optimization: Leverages the strengths of both SQL and NoSQL to minimize latency in high-traffic scenarios.

📂 Project Structure
'''
├── src/main/java/com/amarjo/novaralms
│   ├── auth/          # JWT Security & Identity Management
│   └── course/        # Courses, Videos & Streaming Logic
└── storage/           # Local storage for Media
'''
🚀 Future Roadmap
[ ] Progress Tracking: Implementation of student progress monitoring and course completion percentage calculation.

[ ] PayOn Integration: Integration of the "PayOn" financial gateway with Idempotency Key support for secure, non-duplicate transactions.

[ ] Real-time Notifications: WebSocket-based notifications for new content and enrollment updates.

🚀Run the Project
'''bash
mvn spring-boot:run
'''
👨‍💻 Developer
Yousef Mohammed
Software Engineering Student | Backend & Systems Engineer | Mobile Application Developer
