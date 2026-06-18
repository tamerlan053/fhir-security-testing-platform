## **Professional Bachelor of Applied Computer Science** 

## **Security Evaluation of FHIR Servers Using Automated Attack Scenarios** 

## Tamerlan Aslakhanov 

Promoters: 

Dr. Oliver Krauss University of Applied Sciences Upper Austria Dr. Johan Cleuren PXL University of Applied Sciences and Arts 

**Bachelor paper Academic year 20…-20…** 

## **Professional Bachelor of Applied Computer Science** 

## **Security Evaluation of FHIR Servers Using Automated Attack Scenarios** 

Tamerlan Aslakhanov 

Promoters: 

Dr. Oliver Krauss University of Applied Sciences Upper Austria Dr. Johan Cleuren PXL University of Applied Sciences and Arts 

**Bachelor paper Academic year 20…-20…** 

## **Acknowledgements** 

I would like to express my sincere gratitude to my promoters, Dr. Oliver Krauss from the University of Applied Sciences Upper Austria and Dr. Johan Cleuren from PXL University of Applied Sciences and Arts, for their guidance, feedback, and support throughout this project. Their support and feedback were very valuable and helped me improve both the platform and the thesis. 

I would also like to thank Christoph Praschl from the Research and Development department of the University of Applied Sciences Upper Austria for his supervision and support during the traineeship. His practical guidance and involvement in the day-to-day progress of the project were invaluable. 

Finally, I would like to thank my family and friends for their continued encouragement and patience during the course of this bachelor’s programme. 

Title Bachelor paper – Tamerlan Aslakhanov 

ii 

## **Abstract** 

The traineeship takes place within the Research and Development (R&D) department of the University of Applied Sciences Upper Austria. The department is involved in applied research projects carried out in close cooperation with industry and other partners. Its main goal is to solve real-world problems and develop solutions that can be directly used in practice. 

Fast Healthcare Interoperability Resources (FHIR) is a widely used standard for exchanging medical data via Representational State Transfer (REST) Application Programming Interfaces (APIs), and its use keeps growing. With that, the security of FHIR servers becomes more important, especially for validating incoming data and handling authentication correctly. This work focuses on the development of a platform aimed at testing the security of such servers by executing predefined attack scenarios. The platform automates the testing process and collects structured results, which can be used for further analysis of how FHIR servers handle potentially malformed or manipulated requests. 

The platform is built with Spring Boot on the back-end and Angular on the front-end. PostgreSQL is used for storing the data. The system is built step by step, starting with basic communication with FHIR servers and slowly expanding towards automated testing for future results analysis. Standard software engineering practices are applied, including REST API design, modular architecture, and validation mechanisms. 

The research part of the project focuses on the security of publicly accessible FHIR REST APIs. It investigates how validation and authentication mechanisms are applied across different servers and explores potential weaknesses that may appear when handling malformed or manipulated requests. The goal is to understand the level of security these systems really have and to identify and classify potential weaknesses. 

The evaluation of three publicly accessible FHIR R4 servers shows that covert channel persistence through FHIR extensions is the most consistent weakness, present across all tested implementations. Validation behaviour varies between servers: two correctly reject or sanitise malformed payloads, while one accepts invalid JSON and creates a retrievable resource. Authentication is not enforced on any tested server, which is consistent with their role as intentionally open public sandboxes. The developed classification model improves result interpretation by distinguishing confirmed vulnerabilities from open access policies and inconclusive findings. 

Title Bachelor paper – Tamerlan Aslakhanov 

iii 

## **Table of contents** 

|**Table of contents**|**Table of contents**|
|---|---|
|Acknowledgements ..................................................................................................................................ii||
|Abstract ................................................................................................................................................... iii||
|Table of contents ..................................................................................................................................... iv||
|List of figures ........................................................................................................................................... vi||
|List of tables ........................................................................................................................................... vii||
|List of abbreviations .............................................................................................................................. viii||
|Introduction ............................................................................................................................................. 1||
|I.|Traineeship report ........................................................................................................................... 2|
|1|About the company ......................................................................................................................... 2|
|2|Assignment ...................................................................................................................................... 3|
||2.1<br>Problem description ............................................................................................................... 3|
||2.1.1<br>Situation of the problem ................................................................................................... 3|
||2.1.2<br>Background information .................................................................................................... 3|
||2.1.3<br>Stakeholders ...................................................................................................................... 4|
|3|Technical implementation ............................................................................................................... 5|
||3.1<br>Approach and methodology .................................................................................................. 5|
||3.2<br>Implementation activities ...................................................................................................... 5|
||3.2.1<br>System architecture ........................................................................................................... 5|
||3.2.2<br>FHIR server interaction ...................................................................................................... 6|
||3.2.3<br>Server management and persistence layer ....................................................................... 6|
||3.2.4<br>Attack framework architecture ......................................................................................... 7|
||3.2.5<br>REST API Layer (Backend endpoints) ................................................................................. 7|
||3.2.6<br>Data model ........................................................................................................................ 8|
||3.2.7<br>Attack Scenario Catalogue ................................................................................................. 9|
||3.2.8<br>Result classification model .............................................................................................. 10|
|II.|Research topic ............................................................................................................................... 12|
|1|Research question and problem statement .................................................................................. 12|
||1.1<br>Research question ................................................................................................................ 12|
||1.2<br>Sub-questions....................................................................................................................... 12|
||1.3<br>Problem statement .............................................................................................................. 12|
||1.3.1<br>FHIR and Healthcare Data Exchange ............................................................................... 12|
||1.3.2<br>Security Challenges in Public FHIR Servers ...................................................................... 12|
||1.3.3<br>Project .............................................................................................................................. 13|
||1.4<br>Research Methodology ........................................................................................................ 13|



Title Bachelor paper – Tamerlan Aslakhanov 

iv 

|2|Literature Study ............................................................................................................................. 14|Literature Study ............................................................................................................................. 14|
|---|---|---|
||2.1|Introduction ......................................................................................................................... 14|
||2.2|Selected Sources .................................................................................................................. 14|
||2.2.1|Source 1 - HL 7 FHIR Specification ................................................................................... 14|
||2.2.2|Source 2 – OWASP API Security Top 10 ........................................................................... 15|
||2.2.3|Source 3 – Research Paper .............................................................................................. 15|
||2.3|Validation in REST APIs......................................................................................................... 15|
||2.4|Authentication and Authorisation ....................................................................................... 16|
||2.5|Comparison of Sources ........................................................................................................ 16|
|3|Proof|of Concept............................................................................................................................ 18|
||3.1|Tested Servers ...................................................................................................................... 18|
||3.2|Overall results ...................................................................................................................... 18|
||3.3|Validation behaviour ............................................................................................................ 19|
||3.4|Authentication and open access behaviour ......................................................................... 20|
||3.5|Covert channel behaviour .................................................................................................... 20|
||3.6|Access control behaviour ..................................................................................................... 21|
||3.7|Response body leakage ........................................................................................................ 21|
||3.8|Limitations............................................................................................................................ 22|
|Conclusion ............................................................................................................................................. 23|||
|Reflection||.............................................................................................................................................. 25|
|Bibliography ........................................................................................................................................... 27|||
|Appendices ............................................................................................................................................ 29|||



Title Bachelor paper – Tamerlan Aslakhanov 

v 

## **List of figures** 

Figure 1. Entity relationship diagram of the persistence layer ............................................................... 9 Figure 2. Server Management View ...................................................................................................... 29 Figure 3. Security Test Runner View ...................................................................................................... 29 Figure 4. Security Test Results Overview .............................................................................................. 29 

Title Bachelor paper – Tamerlan Aslakhanov 

vi 

## **List of tables** 

Table 1. Backend REST API Endpoints ..................................................................................................... 8 Table 2. Attack scenario groups .............................................................................................................. 9 Table 3. AttackClassification values ...................................................................................................... 10 Table 4. Tested servers .......................................................................................................................... 18 Table 5. Overall results per server ......................................................................................................... 18 Table 6. Classification per scenario per server ...................................................................................... 19 

Title Bachelor paper – Tamerlan Aslakhanov 

vii 

## **List of abbreviations** 

- **API** Application Programming Interface **DTO** Data Transfer Object **ERD** Entity Relationship Diagram **FHIR** Fast Healthcare Interoperability Resources **HTTP** Hypertext Transfer Protocol **JPA** Java Persistence API **JSON** JavaScript Object Notation 

- **LOINC** Logical Observation Identifiers Names and Codes **OAuth2** Open Authorisation 2.0 **R&D** Research and Development **REST** Representational State Transfer **SQL** Structured Query Language **UI** User Interface **URL** Uniform Resource Locator 

Title Bachelor paper – Tamerlan Aslakhanov 

viii 

## **Introduction** 

Modern healthcare relies heavily on digital infrastructure to store, process, and share medical data. As hospitals and clinics increasingly depend on multiple software systems working in tandem, the question of how well those systems talk to each other has become crucial. When patient records must be transferred between primary care systems and hospital systems, for instance, seamless data exchange can directly affect the quality of care delivered. This connection between different systems is commonly referred to as interoperability, and one of the leading technical standards in use today is Fast Healthcare Interoperability Resources, better known as FHIR. [1] Built on familiar web technologies, FHIR provides a common language that healthcare applications can use to share clinical data in real time. 

In this context, the project focuses on the development of a FHIR Security Testing Platform designed to analyse publicly accessible FHIR servers. Instead of relying on manual inspection, the platform executes automated attack scenarios and records structured results. These results make it possible to observe how individual servers process requests and how effectively validation and access control mechanisms are enforced. 

The goal is to build a clearer picture of the security weaknesses of FHIR REST APIs that are exposed to the open internet. This question has real practical relevance as more healthcare data flows through these endpoints. 

## **I. Traineeship report** 

## **1 About the company** 

The traineeship takes place at the University of Applied Sciences Upper Austria, a public university in Austria that operates several campuses and offers study programs in engineering, business, informatics, health sciences, and media technology. [2] The university strongly focuses on applied learning, where academic knowledge is regularly connected with practical implementation through cooperation with industry partners. The university is also involved in applied research developed in cooperation with both regional and international partners. Many of these projects address practical technological challenges and often combine research activities with software development and system integration. [3] 

The internship itself takes place in the Research and Development (R&D) department. The team works on applied research tasks, prototype development, and the exploration of new technological approaches. In this environment, academic concepts are not only discussed but are also explored through practical implementation and experimentation in real software systems. [4] 

From an IT standpoint, the department works primarily with modern software development practices in the areas of distributed systems, web services, and data exchange standards. Projects frequently involve backend development, REST-based APIs, and the integration of interoperable healthcare systems. Security evaluation and research-driven development also play a central role in the department's technical work. 

The department appears to encourage practical experimentation and collaborative development. Projects are often carried out in small teams, where concepts are tested through working prototypes and evaluated under realistic conditions. The work remains closely tied to practical problems and industry demands. 

## **2 Assignment** 

The internship project focuses on creating a Fast Healthcare Interoperability Resources (FHIR) Security Testing application designed to evaluate the functionality of publicly available FHIR servers. The platform aims to automate security testing by executing predefined attack scenarios and analysing how servers enforce validation and authentication mechanisms. 

## **2.1 Problem description** 

## **2.1.1 Situation of the problem** 

Modern healthcare systems depend heavily on interoperable digital infrastructures to exchange clinical data across organisations and applications. [1] The Fast Healthcare Interoperability Resources (FHIR) standard has become one of the most widely adopted frameworks for this purpose, enabling data exchange through REST-based APIs. Consequently, a growing number of FHIR servers are now reachable over the public internet, enabling healthcare systems to share patient information with considerably greater efficiency. [5] 

This increased exposure, however, raises a set of security concerns that cannot be ignored. Medical data is among the most sensitive categories of personal information. In addition, gaps in validation or authentication can allow unauthorised parties to access or alter clinical records. [6] At the same time, FHIR implementations differ widely in how strictly they apply security controls, which makes it hard to predict how a publicly accessible server will respond to requests it should reject. [7] 

## **2.1.2 Background information** 

Fast Healthcare Interoperability Resources (FHIR) is a standard developed by HL7 for exchanging healthcare data between systems. It describes medical information as modular resources, such as Patient and Observation, which can be exchanged using common web technologies like Hypertext Transfer Protocol (HTTP) and JavaScript Object Notation (JSON). [8] 

FHIR resources serve as the fundamental pieces of healthcare data exchange. Clinical information, such as patient records, laboratory results, and prescriptions, is represented as individual resources within the FHIR framework. These resources can be accessed and manipulated through standard HTTP operations such as GET, POST, PUT, and DELETE. For example, a request to retrieve patient data can be performed using the endpoint /Patient. [9] 

FHIR APIs are commonly built on standard web technologies and, in some cases, are accessible from the public internet. As a result, they may become potential targets for security attacks. [6] If incoming requests are not properly validated or if authentication mechanisms are insufficient, attackers may be able to alter data, submit malformed resources, or access medical information without authorisation. [10] 

For this reason, it is necessary to dig into how FHIR servers handle incoming requests in practice. Such an evaluation helps determine whether validation and security controls are applied correctly. 

## **2.1.3 Stakeholders** 

Several stakeholder groups are involved in the development of a FHIR security testing platform. 

Healthcare organisations, including hospitals and other medical institutions, depend on interoperable systems to exchange patient information across different providers. Since these systems transmit medical data, the security of the FHIR APIs is critical to protecting patient privacy. 

FHIR server administrators are also influenced. These teams are responsible for operating, maintaining, and securing FHIR servers. A security testing platform can help them observe how their systems react to potentially malicious requests and help reveal weaknesses in request validation or authentication mechanisms. 

Healthcare software developers are also affected, as they build applications that rely on FHIR APIs for exchanging clinical data. Results from automated security testing can provide practical insights that help improve the security of their implementations. 

Finally, security researchers and academic institutions form an additional stakeholder group. They are interested in analysing the security properties of publicly accessible healthcare APIs. A testing platform would help to investigate possible vulnerabilities and to build a clear picture of the overall security state of FHIR-based systems. 

## **3 Technical implementation** 

## **3.1 Approach and methodology** 

The FHIR Security Testing Platform uses an automated process in which predefined attack scenarios are executed against selected FHIR servers. Each scenario generates a request intended to test server behaviour under potentially harmful conditions. For example, the system may submit malformed or manipulated resources to determine whether incoming data is validated correctly. 

Attack scenarios are executed through a modular framework. Each attack is implemented as a separate component that can be triggered automatically, making it easier to extend the system with additional scenarios without changing the overall architecture. 

During each test run, the platform sends HTTP requests to the target server and records the corresponding responses. Data such as HTTP status codes and response bodies are stored in a database for later analysis of how the servers handle harmful requests. 

## **3.2 Implementation activities** 

## **3.2.1 System architecture** 

The FHIR Security Testing Platform is implemented as a multi-layer application. This approach improves modularity and makes it easier to develop individual components independently. Through the platform, users can register FHIR servers, run automated security tests, and review the responses returned by the tested systems. 

The system consists of three main parts: a frontend user interface, a backend application layer, and a relational database. In addition, the backend communicates with external FHIR servers to perform security tests and collect the resulting responses. 

The frontend part of the application is implemented using Angular and acts as the main interface for users. Through this interface, users are able to register new FHIR servers, trigger security tests, and visualise test outcomes. The frontend talks to the backend using a REST-based HTTP API. The backend is built with Spring Boot and is split into several layers, including controllers, services, and repositories. The controllers handle incoming requests from the frontend and send back structured responses. 

The frontend includes three main views: server management (/servers), attack runner (/attacks), and cross-server comparison (/compare). Through these views, users can register FHIR servers, execute security tests, and compare the latest results across multiple server implementations. 

The user interface of the platform consists of several views for server management, attack execution, and result comparison. Screenshots of the implemented application are provided in Appendix A. 

The service layer manages most of the logic. These services are responsible for executing different attack scenarios and loading stored information from the database. The database interaction is managed through repository classes using Spring Data Java Persistence API (JPA). In addition, a custom attack framework is included in the backend. It is responsible for running automated security tests against FHIR servers. A key idea behind the framework is that it should be easy to extend. New attack scenarios can be added later on without affecting the existing system architecture. Each scenario represents a specific type of test and is executed automatically during a test run. 

For persistence, a PostgreSQL relational database is used. It stores details about registered FHIR servers, executed test runs, and the results of individual attacks. The mapping between Java objects and database tables is done using JPA together with Hibernate. During execution, the backend communicates with external FHIR servers via HTTP REST requests. Standard FHIR operations are handled using the HAPI FHIR client library. However, for the security tests, a lower-level HTTP client is used, since these tests often require sending manipulated and malformed requests, which gives more direct control over how the requests are constructed. 

## **3.2.2 FHIR server interaction** 

Interaction with FHIR servers is handled in two different ways. One way is used for normal communication with public FHIR servers, and the other one is used for running security tests. This approach makes it possible to work with standard FHIR operations while also testing how servers behave under unusual or incorrect requests. 

The first part uses a FHIR R4-compliant client based on the HAPI FHIR library. In this case, a FhirContext for R4 is configured, and an IGenericClient is created dynamically using the base Uniform Resource Locator (URL) of the target server. With this client, typical REST operations can be performed, for example, requesting resources such as Patient or Observation, or creating a new Patient resource. Since this follows the official FHIR specification and uses typed resource models, it is mainly used to check if the server works correctly under normal conditions. 

The second part is used when the attack scenario simulation is executed. For the security tests, the FHIR client is skipped. Instead, the platform sends HTTP requests directly using Spring’s RestTemplate. This makes it easier to build requests manually, including cases where the JSON is incorrect or intentionally modified. Such requests are not allowed by the HAPI client, so this lowerlevel approach gives more control. In most cases, the requests are sent to endpoints like /Patient or /Observation, using the base URL of the selected server. 

The advantage of this separation is that normal FHIR communication and security testing do not interfere with each other. It also makes it easier to observe how a server behaves in different situations, both when the request is valid and when it is not. 

## **3.2.3 Server management and persistence layer** 

The platform also handles data storage and registered servers. This includes both configuration details and the results produced during testing. For storage, a PostgreSQL database is used, while Spring Data JPA is used to access the data and map it to Java objects. 

The testing workflow is represented by several entities. FhirServer stores basic information about each server, such as its name and base URL. When a test is executed, a TestRun entry is created and linked to that server. It also records when the execution started. The results of attacks are saved separately as TestResult objects. These contain the HTTP status code, the response body, and whether something vulnerable was detected. The AttackScenario entity is used to describe each attack, including its name, a short explanation, and a severity level. 

Spring Data JPA repositories are used for accessing the database. The application also uses a REST interface for managing FHIR servers. Users can add new servers, view existing ones, or remove them. 

The logic behind these actions is placed in the service layer, so the interaction with the database stays consistent. 

The registered servers are stored in the database and later used during attack execution. Communication with the selected FHIR server is then handled through separate client components. 

## **3.2.4 Attack framework architecture** 

The application is designed in a modular way, which makes it possible to run different security tests consistently and scale when needed. The architecture follows a separation of concerns, so in practice, it is easier to introduce new attack scenarios without having to modify the core logic. 

A key part of the design is the ExecutableAttack interface, which provides a shared structure for all attack scenarios. Each implementation follows this interface and includes its own execution logic through a standard method that returns an AttackResult. The result contains the HTTP status code, the response body, and a vulnerability flag, allowing outputs to stay consistent and easy to compare. 

Attack implementations are registered automatically using Spring’s dependency injection. Each attack exists as an independent component, while the AttackRegistry collects all classes implementing ExecutableAttack at runtime. Because of that, adding a new attack is simple; it only requires creating a new class, without touching existing code. 

The execution itself is handled by AttackExecutorService. It iterates over all registered scenarios and runs them against a selected FHIR server. During each run, the framework stores database records that link scenarios to their results. It also keeps additional metadata, such as the name, description, and severity of each scenario, which can later be reused in future test executions. 

Since definition, registration, and execution of attacks are separated, the framework remains flexible and relatively easy to work with. This becomes especially useful when new test cases are added frequently and need to be compared, without affecting the stability of the overall system. 

## **3.2.5 REST API Layer (Backend endpoints)** 

Backend provides a REST-based API that is being used by the frontend side of the application for managing the list of FHIR-servers targeted for testing. Data exchange between frontend and backend is executed by HTTP in JSON format, which simplifies integration and helps the frontend to work with the results regardless of the specific implementation. 

Backend responses are returned as structured Data Transfer Objects (DTOs), so that the data is suitable for User Interface (UI) presentation and further analysis. For test run results, the API returns execution metadata (run identifier, server, start time) and a list of results for each attack scenario, where each check records the scenarioName, statusCode, calculated classification, vulnerable flag, as well as an explanation and severity level. 

Table 1 provides an overview of the most important backend endpoints used in the application. 

Table 1. Backend REST API Endpoints 

|Table 1. Backend REST API Endpoints||
|---|---|
|**Endpoint**|**Purpose**|
|POST/api/servers|Register a new FHIR server usingname and base URL.|
|GET/api/servers|Retrieve all registered FHIR servers.|
|DELETE/api/servers/{id}|Remove a registered server.|
|POST/api/attacks/run/{serverId}|Start a new securitytest run for the selected server.|
|GET/api/results/{testRunId}|Retrieve full results of a specific test run.|
|GET/api/attacks/runs/{serverId}|Retrieveprevious test runs for a selected server.|
|GET<br>/api/results/compare?serverIds=...|Compare the latest test runs across multiple servers.|
|GET<br>/api/results/row/{testResultId}|Retrieve full response body and vector details for a single test<br>result.|



## **3.2.6 Data model** 

The persistence layer uses a relational database to store both registered target FHIR servers and test runs. Figure 1 below shows the Entity Relationship Diagram (ERD) of four tables: fhir_server, test_run, attack_scenario, and test_result. These tables represent the testing workflow: selecting a server, executing the attack and recording detailed outcomes for each scenario. 

The attack scenario table keeps attack metadata (name, description, and severity level) separate from the test_result table, which stores HTTP status code, computed classification, vulnerability flag, corresponding reason, attack vector identifiers, and leakage exposure analysis results. The attack_vector_ids field stores stable probe tags identifying the specific HTTP vectors and FHIR fields targeted by a scenario (for example, http.post.patient or fhir.extension.covert_channel), while leakage_exposure captures the result of automated response body analysis using values such as NONE, VERBOSE_ERROR_BODY, or IMPLEMENTATION_DETAIL_LEAK. 

One FHIR server (fhir_server) can have many test runs (test_run), each test run contains many test results (test_result), and each test result references exactly one attack scenario (attack_scenario). 

Figure 1. Entity relationship diagram of the persistence layer 

## **3.2.7 Attack Scenario Catalogue** 

The platform implements twelve attack scenarios that dig into different spaces of FHIR server security. Each scenario follows the same fundamental pattern: a crafted HTTP request is sent to the target server, and the response is analysed, then a follow-up GET request is forwarded, where necessary. The follow-up request is to verify whether any changes are actually stored. This two-step approach reduces false positives by ensuring that a vulnerability is only reported when its significance can be confirmed, not when an unexpected status code is returned. The full specification of each individual scenario is provided in Appendix B. 

The twelve scenarios are organised into four thematic groups as shown in Table 2. 

Table 2. Attack scenario groups 

|**Group**|**Scenarios**|**Security property tested**|
|---|---|---|
|**Input validation**|Malformed JSON Request, Metadata<br>Manipulation, Unexpected Payload<br>Injection|Whether the server correctly rejects<br>structurally and semantically invalid<br>request bodies|
|**Covert channel**<br>**detection**|Extension Fields Misuse, Contained<br>Resource Smuggling, Encoded Hidden<br>Data|Whether valid-but-unusual FHIR<br>fields can be used to store and<br>retrieve hidden data|



|**Group**|**Scenarios**|**Security property tested**|
|---|---|---|
|**Authentication**|Invalid Credentials Access Test, Open<br>Endpoint Detection, Authenticated<br>Token Isolation|Whether the server enforces<br>authentication consistently across<br>endpoints|
|**Access control**|Cross-Patient Access, Unauthorised<br>Write / ID Tampering, Observation<br>Bundle / Duplicate Clinical|Whether unauthenticated reads and<br>writes to patient-specific resources<br>are rejected.|



The input validation group is responsible for parsing incoming request bodies and testing field constraints. The covert channel exploits fields that are structurally allowed by the FHIR specification, but can contain arbitrary payloads. The authentication group tests credentials directly and compares the security methods declared by the server with those that are truly enforced. The access control group creates real patient resources during the rest run and then attempts to read or modify them without authorisation, producing behavioural proof rather than relying on status codes. 

## **3.2.8 Result classification model** 

Result classification is very important because, without it, the report would simply state that a target is either vulnerable or not vulnerable. This approach can lead to false conclusions because the same observable behaviour can have totally different meanings depending on the deployment context. A server that allows unauthenticated reads may be an intentionally open sandbox, a misconfigured production system, or simply a demo instance with no sensitive data. If all of these are collapsed into a single “vulnerable” label, this makes the output difficult to act on. 

To avoid this problem, the platform uses an extended classification taxonomy. Every attack scenario produces an AttackResult record containing six fields: the HTTP status code returned by the target server, the raw response body, an AttackClassification value, a human-readable reason string, an AttackSeverity value, and a boolean vulnerable flag derived directly from the classification. The classification and severity fields are stored in the test_result table and are used both in the per-run results view and in the cross-server comparison matrix. 

In addition to classification and severity, each result carries two supplementary fields introduced to support deeper analysis. The attackVectorIds field contains stable comma-separated tags that identify the specific HTTP methods, endpoints, and FHIR fields targeted by each probe, enabling filtering and cross-run comparison at the vector level rather than only at the scenario level. The leakageExposure field records the outcome of automated response body analysis performed by ResponseBodyLeakageAnalyzer. Bodies are scanned for patterns associated with implementation detail disclosure, including Java stack frames, exception class names, SQL driver strings, infrastructure paths, and unusually large diagnostic payloads. Results are assigned one of three tiers: NONE, VERBOSE_ERROR_BODY, or IMPLEMENTATION_DETAIL_LEAK. 

Table 3. AttackClassification values 

**Classification Meaning Typical trigger** 

|SECURE|Server behaved as<br>expected|4xx rejection of invalid input; 401/403 on protected<br>endpoints|
|---|---|---|
|VULNERABLE|Confirmed security<br>weakness|Unauthorised access succeeds on a server advertising<br>OAuth; a malformed payload creates a retrievable<br>resource|
|OPEN_POLICY|Access is open by<br>design|Anonymous read succeeds; no OAuth/SMART declared in<br>server metadata|
|INCONCLUSIVE|Outcome cannot be<br>determined|Server returns 500; follow-up GET requires credentials;<br>non-standard response|



The difference between VULNERABLE and OPEN_POLICY depends on the context discovered at runtime. Before classifying authentication and access control attacks, the platform retrieves /.wellknown/smart-configuration and /metadata from the target server and checks whether the document declares OAuth or SMART support. If a 200 response is received to an unauthenticated read, it is classified as VULNERABLE if OAuth is specified, and as OPEN_POLICY if it is not. This prevents the public demo server from giving false positives that claim to enforce authentication but do not. 

The VULNERABLE flag is stored for each result. It is set to true only for confirmed vulnerabilities. This flag is used for metrics like the total number of vulnerabilities and server comparisons. Other findings, such as OPEN_POLICY or INCONCLUSIVE, are still shown in the results but are not counted as real vulnerabilities. 

Severity is evaluated separately from classification using the AttackSeverity scale: INFO, LOW, MEDIUM, HIGH, and CRITICAL. SECURE and OPEN_POLICY results receive INFO, while INCONCLUSIVE receives LOW. VULNERABLE results are assigned a severity based on potential impact, for example, an invalid credential token weakness is CRITICAL, but hidden metadata persistence is LOW. For scenarios with multiple checks, the platform applies a worst-case rule by selecting the highest-risk classification and highest severity, ensuring that a single failed test is still reflected in the final result. 

## **II. Research topic** 

## **1 Research question and problem statement** 

## **1.1 Research question** 

To what extent do FHIR REST APIs enforce secure validation and authentication mechanisms against structured and covert attack vectors? 

## **1.2 Sub-questions** 

How do FHIR REST APIs validate incoming requests, and what validation mechanisms are they applying? 

Which authentication mechanisms are implemented by FHIR servers? 

To what extent are FHIR REST APIs vulnerable to attacks? 

## **1.3 Problem statement** 

Today, with the growth and ongoing development of healthcare systems, the secure storage and transmission of medical data have become very important. The term interoperability refers to the ability of different systems to exchange information in real time. This ability has become important in the medical domain, where security, availability, and the speed of data exchange directly affect the quality and reliability of healthcare services. [1] 

## **1.3.1 FHIR and Healthcare Data Exchange** 

There are several standards for exchanging clinical data; however, one of the most widely adopted today is Fast Healthcare Interoperability Resources (FHIR). FHIR is built on REST principles and makes use of common web technologies, including HTTP and JSON, which allow healthcare applications to exchange data in a structured way [11]. As publicly accessible FHIR-based REST APIs become more widespread, the associated security risks tend to increase as well. In this context, validation, authentication, and authorisation mechanisms need to operate reliably to maintain secure data exchange and reduce the likelihood of unauthorised access to sensitive medical information. 

## **1.3.2 Security Challenges in Public FHIR Servers** 

Although FHIR is widely implemented, publicly accessible FHIR servers do not always apply validation and authentication controls with the same level of strictness. Differences in how incoming requests are processed, rejected, or accepted may lead to uncertain protection levels across systems. In some cases, authentication mechanisms may be present but not enforced across endpoints, while validation procedures may fail to detect malformed or manipulated resource structures. The absence of a clear method for systematically evaluating systems creates technical uncertainty regarding the actual security of publicly exposed FHIR REST APIs. [7] 

Although FHIR is widely used in the field of interoperability, there is limited research providing comparative analyses of validation and authentication mechanisms within publicly accessible FHIR environments. This lack of structured evaluation makes it more difficult to assess and systematically study the security posture of such systems. [12] 

## **1.3.3 Project** 

The project includes the development of a FHIR Security Testing Platform within a Research and Development (R&D) environment. The purpose of this platform is to execute automated attack scenarios against publicly accessible FHIR servers and collect structured test results. The results help refine the attack execution logic and support the classification and evaluation of identified vulnerabilities. This helps better understand how reliably and securely publicly accessible FHIR REST APIs operate. 

## **1.4 Research Methodology** 

This research evaluates the security of FHIR REST APIs through a literature review and practical testing. A dedicated security testing platform is developed and used for hands-on testing of multiple FHIR REST APIs. 

The first phase of the research consists of a literature study on validation mechanisms, authentication methods, and known security issues in FHIR systems. The sources used in this study are scientific articles, official FHIR documentation, and security guidelines. 

Moreover, an experimental setup is used in which multiple FHIR REST APIs are tested through automated attack scenarios. These include malformed requests to evaluate validation, authentication tests using invalid or missing credentials, access control tests, and covert data injection techniques. The system records and analyses the responses from each server. 

The collected data is evaluated based on how the servers respond to the tests. This includes checking whether requests are accepted or rejected, how authentication is handled, and whether any sensitive data is exposed. The results are analysed to identify possible weaknesses in each server. 

The research questions are approached by studying validation mechanisms and authentication strategies, and at the same time reviewing the vulnerability of FHIR REST APIs to different attacks. 

## **2 Literature Study** 

## **2.1 Introduction** 

This literature study explores risks of FHIR REST APIs, with a focus on validation and authentication mechanisms. Considering the fact that FHIR is getting adopted more and more across healthcare systems, there is still limited research that analyses the security of FHIR APIs in real-world scenarios. [12] 

Because of this lack of FHIR-specific literature, this study approaches the topic from a broader perspective by examining general REST API security principles. Since FHIR APIs are based on REST architecture, many common API security risks and mechanisms are directly applicable to FHIR systems. [9] 

Sources like the FHIR specification mainly describe how validation and authentication work. On the contrary, general API security guidelines and research papers focus on common vulnerabilities and implementation issues found in real-world systems. 

Because of that, both FHIR-related sources and broader REST API security literature are considered together. The point is to better understand how validation and authentication actually behave in practice, not just how they are supposed to work, and also to notice recurring security weaknesses that might affect FHIR APIs as well. These observations are then used as a starting point for the practical part of the project, where FHIR servers are checked through automated security testing. 

## **2.2 Selected Sources** 

In order to analyse the security of FHIR REST APIs, three main types of sources are selected. These include the official FHIR specification, general API security guidelines, and academic research. Each of these sources provides a different perspective on API security, which allows for a more complete understanding of the topic. 

## **2.2.1 Source 1 - HL 7 FHIR Specification** 

The FHIR specification, created by HL7, sets the official standard for exchanging healthcare data using REST-based APIs. It explains how resources are structured and also how different systems are supposed to communicate with each other. [5] 

From a security point of view, it defines how validation and authentication should operate in theory. Servers are expected to validate incoming resources so they match the defined structure, data types, and constraints. [13] That includes checking required fields, making sure values follow the right format, and generally that the resource is consistent. 

When it comes to authentication, the FHIR specification does not fix one specific method. It supports different approaches, for example, Open Authorisation 2.0 (OAuth2) or other token-based authentication mechanisms. This kind of flexibility puts the responsibility on developers to make sure that the implementation is secure. [10] 

Overall, the specification describes how systems should behave and gives instructions for implementing them securely. 

## **2.2.2 Source 2 – OWASP API Security Top 10** 

The OWASP API Security Top 10 is a well-known set of guidelines that lists the most common security risks in modern APIs. [10] Compared to the FHIR specification, which is more about design and how things should be structured, OWASP is more focused on what actually goes wrong in practice and the kinds of implementation mistakes that happen. 

Some of the risks mentioned are clearly connected to validation and authentication. For example, broken authentication (API2) refers to situations where authentication is implemented poorly, which can let attackers bypass access controls or even act as other users. Another case is broken objectlevel authorisation (API1), where weak or absent access control checks may allow unauthorised users to access data. 

Validation is not presented as its own category, but still, several risks are related to it. Server-Side Request Forgery (API7), for instance, can happen when user input is not validated properly. Also, security misconfiguration (API8) can lead to problems if requests are not handled correctly. 

Overall, OWASP gives a more practical perspective on API security. It looks at how systems fail in practice, instead of only explaining how they should behave in theory. 

## **2.2.3 Source 3 – Research Paper** 

The chosen paper is a systematic literature review published in JMIR Medical Informatics in 2021, focusing on how FHIR is used and adopted in healthcare. [12] It goes through a large number of scientific articles and gives a general overview of current developments, and also some of the challenges connected to FHIR. 

From what is discussed there, FHIR adoption is clearly increasing in the healthcare domain, and it is often seen as an important standard for interoperability. FHIR is still not fully mature and continues to evolve, which makes implementation harder than expected. [12] 

Security is not really the main focus of the paper, but still, it gives some useful insight into how FHIR is used in practice. For example, implementations can differ quite a lot, and another point is that there is still not much detailed research looking at specific areas like security evaluation. [12] 

So in the end, it becomes clear that although FHIR describes how systems should behave, there is still a lack of research focusing on their actual security in practice. 

## **2.3 Validation in REST APIs** 

Validation is a very important part of REST API security. It guarantees that incoming data is correct, consistent, and safe to handle. In REST systems, validation is usually applied to incoming requests, where the structure, format, and content are checked against expected requirements. [14] 

For FHIR APIs, it is crucial since medical data must comply with strict structural rules. According to the FHIR specification, resources must be validated against defined structures, data types, and constraints to ensure correctness and interoperability. [15] This includes checking required fields, value formats, and making sure the resource stays consistent overall. 

However, in practice, validation in real REST APIs is not always implemented properly. Some systems only apply partial validation or depend on client-side checks, which are relatively easy to bypass. Because of this, incorrect or modified requests can still reach the server and end up being processed. [10] 

Such insufficient validation can lead to serious problems. It can cause incorrect data to be stored or lead to unpredictable system behaviour. In medical systems, this is especially critical, as it may affect the integrity of medical records. [7] That is why validation should be considered not only as a mechanism for checking correctness, but also as an important element of REST API security. 

## **2.4 Authentication and Authorisation** 

Authentication and authorisation are very important when it comes to REST API security. With authentication, the system verifies who is making the request. Authorisation then controls what this user or system can do next. 

In most REST-based systems, authentication is typically implemented using mechanisms such as OAuth2, API keys, or token-based approaches. As REST APIs use HTTP, these mechanisms help secure endpoints and manage access to sensitive data. [6] 

The FHIR API specification does not require the use of one specific authentication method, but instead allows any implementation, depending on system requirements. So developers can choose suitable security mechanisms, but it also affects how consistently security is applied across different systems. [13] 

According to the OWASP API Security Top 10, authentication-related issues remain one of the most common causes of API vulnerabilities. Broken authentication (API2) can allow attackers to bypass login mechanisms, reuse tokens, or act as legitimate users. [10] This can lead to unauthorised access to sensitive data, which is especially critical in healthcare systems. 

Another issue is that authorisation is not always enforced consistently across all endpoints. Some APIs correctly restrict access to certain resources, while others do not apply sufficient access checks. This kind of inconsistency increases the overall risk and shows why it is important to evaluate authentication and authorisation mechanisms in practice. [10] 

## **2.5 Comparison of Sources** 

The selected sources consider the topic of API security from different points of view. Each of these sources provides a strong foundation for evaluating the security of publicly accessible FHIR REST APIs. 

The HL7 FHIR specification explains how FHIR systems are expected to function, how resources should be structured, and which security mechanisms can be implemented. It defines standards and focuses more on how implementations should behave than on common real-world failures. 

The OWASP API Security Top 10, on the other hand, explains how attacks happen in practice. It identifies common weaknesses found in deployed APIs, such as broken authentication, insufficient authorisation, and security misconfiguration. This makes it highly useful when analysing attack scenarios and understanding how systems may fail in practice. 

The selected academic paper supports the topic from a wider research standpoint. It shows that adoption of FHIR continues to increase, but at the same time, it indicates that research into the security evaluation of public servers is still limited. This highlights an existing research gap. 

When comparing the three sources together, it is obvious that none of them alone gives a complete picture. The FHIR specification explains the expected design, OWASP explains common implementation risks, and academic literature describes adoption trends and unresolved challenges. But these three perspectives together create a strong foundation for the practical part of this project. 

For that reason, the developed testing platform uses ideas from all three areas. It evaluates if publicly accessible FHIR servers follow expected standards, whether they have common API vulnerabilities, and how they behave in real testing conditions. 

## **3 Proof of Concept** 

## **3.1 Tested Servers** 

Three publicly accessible FHIR R4 servers are selected for evaluation. All three are well-known test and reference servers listed on the official HL7 Confluence page of public FHIR endpoints. [16] None of them is a production system, and they are provided for development and research purposes. The servers are tested using the platform developed as part of this project. 

Table 4. Tested servers 

|**Server**|**Base URL**|**Implementation**|
|---|---|---|
|HAPI FHIR|https://hapi.fhir.org/baseR4|Java / HAPI FHIR library|
|SMART Health IT|https://r4.smarthealthit.org|Java / HAPI FHIR library|
|Firely|https://server.fire.ly/r4|C# / Firely SDK|



## **3.2 Overall results** 

Each server is tested with all twelve attack scenarios. The results are summarised in Table 5. 

Table 5. Overall results per server 

|**Server**|**VULNERABLE**|**OPEN_POLICY**|**SECURE**|**INCONCLUSIVE**|
|---|---|---|---|---|
|HAPI FHIR|2|3|4|3|
|SMART Health IT|2|4|4|2|
|Firely|3|3|4|2|



No server achieves a fully secure result across all scenarios. HAPI FHIR produces two VULNERABLE results, three OPEN_POLICY classifications, four SECURE results, and three INCONCLUSIVE findings. SMART Health IT shows two VULNERABLE results, four OPEN_POLICY classifications, four SECURE results, and two INCONCLUSIVE findings. Firely produces the highest number of VULNERABLE outcomes with three cases, alongside three OPEN_POLICY classifications, four SECURE results, and two INCONCLUSIVE findings. 

The dominant classification across all three servers is OPEN_POLICY, which reflects their nature as intentionally open public sandbox environments. None of the three servers declares OAuth or authentication requirements in the rest[].security field of their CapabilityStatement. HAPI FHIR and Firely return an empty security object, while SMART Health IT declares only "cors": true. The platform, therefore, correctly classifies their open access behaviour as OPEN_POLICY. The VULNERABLE findings are limited to specific technical weaknesses that exist independently of the authentication policy. 

Table 6 provides a detailed breakdown of the classification produced by each scenario on each server. 

Table 6. Classification per scenario per server 

|**Scenario**|**HAPI FHIR**|**SMART Health IT**|**Firely**|
|---|---|---|---|
|Malformed JSON Request|SECURE|SECURE|VULNERABLE HIGH|
|Metadata Manipulation|SECURE|SECURE|SECURE|
|Unexpected Payload Injection|SECURE|SECURE|SECURE|
|Extension Fields Misuse|VULNERABLE<br>MEDIUM|VULNERABLE<br>MEDIUM|VULNERABLE<br>MEDIUM|
|Contained Resource<br>Smuggling|VULNERABLE<br>MEDIUM|VULNERABLE<br>MEDIUM|SECURE|
|Encoded Hidden Data|SECURE|SECURE|VULNERABLE LOW|
|Invalid Credentials Access Test|INCONCLUSIVE|INCONCLUSIVE|INCONCLUSIVE|
|Open Endpoint Detection|OPEN_POLICY|OPEN_POLICY|OPEN_POLICY|
|Authenticated Token Isolation|INCONCLUSIVE|INCONCLUSIVE|INCONCLUSIVE|
|Cross-Patient Access|OPEN_POLICY|OPEN_POLICY|OPEN_POLICY|
|Observation Bundle /<br>Duplicate Clinical|INCONCLUSIVE|OPEN_POLICY|SECURE|
|Unauthorised Write / ID<br>Tampering|OPEN_POLICY|OPEN_POLICY|OPEN_POLICY|



## **3.3 Validation behaviour** 

The input validation group produces the most differentiated results across the three servers. 

HAPI FHIR and SMART Health IT are classified as SECURE across all three validation scenarios, but through different mechanisms. In some cases, such as Malformed JSON Request, both servers explicitly reject the payload with a 4xx status code. In other cases, such as Metadata Manipulation and Unexpected Payload Injection, both servers return HTTP 201 but do not persist the injected content. The markers are absent on follow-up GET, indicating that the servers accept the request but sanitise unrecognised fields before storage. Both outcomes are classified as SECURE since no injected data is retrievable. 

Firely behaves differently in one scenario. The Malformed JSON Request attack submits two payloads: a truncated JSON body and a body with a trailing comma. Firely correctly rejects the truncated variant with a 4xx response, but returns HTTP 201 for the trailing comma and creates a retrievable Patient resource, confirmed by a follow-up GET. This indicates that Firely’s parser accepts and stores a resource constructed from syntactically invalid JSON, which is classified as VULNERABLE with HIGH severity. The Metadata Manipulation and Unexpected Payload Injection scenarios are classified as SECURE on Firely, following the same sanitisation pattern observed on the other two servers. 

Overall, the validation group reveals that all three servers apply input handling, but the approach differs: explicit rejections via 4xx is the stricter behaviour, while silent sanitisation still achieves a SECURE outcome as long as injected content is not persisted. 

## **3.4 Authentication and open access behaviour** 

The authentication group produces consistent results across all three servers: one OPEN_POLICY outcome on Open Endpoint Detection and one INCONCLUSIVE outcome on the Invalid Credentials Access Test. 

The Open Endpoint Detection scenario confirms that none of the three servers enforces authentication. Unauthenticated GET /Patient requests return HTTP 200 on all three servers. Since none of the servers declares OAuth in their CapabilityStatement security field, this behaviour is classified as OPEN_POLICY - consistent with the intentionally open design of public sandbox environments. 

The Invalid Credentials Access Test produces INCONCLUSIVE on all three servers. The OAuth token endpoint sub-probe cannot be executed because no token endpoint URL is discoverable from the metadata, which is expected given the absence of OAuth declarations. The remaining sub-probe requests with invalid Basic credentials and forged Bearer tokens succeed with HTTP 200, which is classified as OPEN_POLICY since no authentication is advertised. The combined result is INCONCLUSIVE because the token endpoint sub-probe could not complete, leaving that specific vector unverified. 

## **3.5 Covert channel behaviour** 

The covert channel group produces the most consistent VULNERABLE findings across all three servers, and also the clearest differentiation between server implementations. 

The Extension Fields Misuse scenario is classified as VULNERABLE with MEDIUM severity on all three servers. A custom FHIR extension with an arbitrary URL and a unique marker value is submitted via POST /Patient and confirmed retrievable via follow-up GET on every server. Since the FHIR specification permits arbitrary extensions, servers are not required to reject them. However, this result demonstrates that all three servers can be used to store and retrieve arbitrary data through the extension mechanism without any form of access control. 

The Contained Resource Smuggling scenario splits the servers. HAPI FHIR and SMART Health IT accept a Patient payload containing an embedded Binary resource and persist it, resulting in VULNERABLE with MEDIUM severity. Firely rejects the payload with HTTP 400 and is classified as SECURE, indicating stricter validation of the contained array. 

The Encoded Hidden Data scenario also produces different results. HAPI FHIR and SMART Health IT normalise or strip the unicode-encoded meta.tag marker before storage, resulting in SECURE. Firely persists the marker and returns it on follow-up GET, resulting in VULNERABLE with LOW severity. 

Overall, all three servers are vulnerable to at least one covert channel scenario. The extension mechanism represents the most consistent finding, present across all three implementations. 

## **3.6 Access control behaviour** 

The access control group produces OPEN_POLICY outcomes on all three servers for both scenarios, which is consistent with their open sandbox nature. 

The Cross-Patient Access scenario creates two Patient resources and attempts to read the victim's record via GET /Patient/{victimId} and an associated Observation via GET /Observation?subject=Patient/{victimId}. Both requests succeed with HTTP 200 on all three servers. Since no OAuth is advertised, these outcomes are classified as OPEN_POLICY rather than VULNERABLE, meaning that the servers make no claim of access isolation and their behaviour is consistent with that declaration. 

The Unauthorised Write / ID Tampering scenario issues an unauthenticated PUT /Patient/{id} with a modified name field containing a unique marker. The modification persists on all three servers, confirmed by a follow-up GET. Again, since no authentication is advertised, this is classified as OPEN_POLICY. The finding is important because in a deployment context where authentication is expected, the same behaviour would be classified as VULNERABLE with CRITICAL severity. 

The Observation Bundle / Duplicate Clinical scenario submits a FHIR transaction bundle containing three structurally identical Observation entries for the same patient, each using the Logical Observation Identifiers, Names and Codes (LOINC) code. The results differ across the three servers. Firely rejects the bundle with HTTP 400, classified as SECURE, giving the strictest response observed across all three servers on this scenario, indicating that Firely enforces validation at the bundle level. SMART Health IT accepts all the observations in a single transaction and returns HTTP 200 with three individual 201 Created responses, classified as OPEN_POLICY since no OAuth is advertised. HAPI FHIR returns an unexpected HTTP 302 redirect in response to the bundle POST, resulting in INCONCLUSIVE, meaning that the platform cannot determine whether the bundle was processed, rejected, or redirected for further handling. 

## **3.7 Response body leakage** 

The platform's response body leakage analyser evaluates every response for patterns associated with implementation detail disclosure, including stack frame indicators, Java exception class names, SQL driver strings, file system paths, and large diagnostic payloads. Results are assigned one of three tiers: NONE, VERBOSE_ERROR_BODY, or IMPLEMENTATION_DETAIL_LEAK. 

Two of the three servers produce at least one non-NONE leakage result. Both HAPI FHIR and SMART Health IT receive a VERBOSE_ERROR_BODY classification on the Unauthorised Write / ID Tampering scenario. In both cases, the response body contains large HTML-formatted output rendered by the server’s built-in web interface, which exceeds the size threshold used by the analyser to flag verbose. responses. This type of output would be inappropriate in a production deployment where plain JSON error responses are expected. Firely returns clean JSON responses throughout all twelve scenarios and receives NONE on every result. 

No IMPLEMENTATION_DETAIL_LEAK tier is triggered on any server within the tested attack surface, indicating that none of the three servers exposes stack traces, exception class names or infrastructure-level strings in their responses. 

## **3.8 Limitations** 

Several factors constrain the interpretation of these results. 

First, all three servers are explicitly designated as public sandbox environments. The OPEN_POLICY classification correctly distinguishes their open access from genuine security misconfigurations, but it cannot determine whether the absence of authentication declarations is intentional or an oversight in server configuration. 

Second, the Invalid Credentials Access Test returns INCONCLUSIVE on all three servers because no OAuth token endpoint is discoverable. The token issuance mechanism, therefore, cannot be evaluated, which represents a gap in the authentication coverage. 

Third, the response body leakage analysis is heuristic. Servers that intentionally return verbose diagnostic output for developer convenience may avoid triggering the patterns, while unusual but benign responses could potentially match them. 

## **Conclusion** 

This project set out to answer the following research question: To what extent do FHIR REST APIs enforce secure validation and authentication mechanisms against structured and covert attacks? 

To answer this question, a full-stack security testing platform is developed and used to evaluate three publicly accessible FHIR R4 servers: HAPI FHIR, SMART Health IT, and Firely. The platform executes twelve automated attack scenarios across four groups: input validation, covert channel detection, authentication, and access control. Each result is classified using an extended four-level taxonomy: SECURE, VULNERABLE, OPEN_POLICY, and INCONCLUSIVE. 

Validation is the area where the tested servers perform best. HAPI FHIR and SMART Health IT correctly reject or sanitise all tested malformed and injected payloads across all three validation scenarios. Firely handles most validation scenarios correctly but accepts a trailing-comma JSON payload and creates a retrievable resource from it, classified as VULNERABLE with HIGH severity. This aligns with the OWASP API Security Top 10 observation that validation failures in real deployments are sometimes incomplete. Therefore, servers may enforce some constraints while remaining permissive on others. 

Authentication and access control produce consistent OPEN_POLICY outcomes across all three servers. None of the servers formally declare OAuth or SMART requirements in their CapabilityStatement security field, and none enforce authentication on any tested endpoint. Unauthenticated reads, writes, and cross-patient data access all succeed on every server. The platform’s classification model correctly identifies this and OPEN_POLICY rather than VULNERABLE, since no authentication is advertised. This distinction is a key methodological contribution: an earlier version of the OAuth detection logic relies on keyword-based heuristics that produce false positives. After replacing the heuristic with strict parsing of the rest[].security.extension block, the results accurately reflect intentional open sandbox design rather than misconfiguration. 

Covert channel persistence is the most consistent confirmed weakness across all three servers. The FHIR extension mechanism allows arbitrary data to be stored and retrieved on all three implementations – the only VULNERABLE finding present across every server. Contained resource smuggling is effective on HAPI FHIR and SMART Health IT, while Firely rejects such payloads. Encoded meta.tag persistence affects only Firely. 

Transaction bundle behaviour reveals important differences between implementations. SMART Health IT accepts a transaction bundle containing three structurally identical Observations for the same patient without deduplication, classified as OPEN_POLICY. HAPI FHIR returns as an unexpected HTTP 302 redirect, resulting in INCONCLUSIVE. Firely rejects the bundle entirely with HTTP 400, classified as SECURE, providing the strictest response across all three servers for this scenario. 

Response body leakage is detected on two of the three servers. Both HAPI FHIR and SMART Health IT produce VERBOSE_ERROR_BODY leakage in the Unauthorised Write scenario, where the response body contains large HTML-formatted diagnostic output that is inappropriate in a production deployment. Firely returns clean JSON responses throughout and produces no leakage findings. 

The Authenticated Token Isolation scenario returns INCONCLUSIVE on all three servers because no bearer token is configured in the test environment. This scenario is designed for servers that enforce OAuth token-level patient isolation and requires environment-specific configuration to produce a meaningful result. It represents a gap in the current evaluation that future work should address by testing against OAuth-enforcing production servers. 

Taken together, the results show that covert channel persistence through the FHIR extension mechanism is the most consistent weakness, present on every tested server regardless of implementation. Validation strictness and bundle handling vary by implementation, with Firely showing stricter behaviour in both areas. Authentication and access control gaps are consistent but reflect intentional open sandbox design rather than security failures. 

The research component directly influences the platform's development. The literature review shapes the four attack groups based on OWASP API1 and API2 risk categories. Practical testing reveals a false positive problem in the OAuth detection heuristic, leading to a concrete code improvement. The JMIR finding that FHIR security evaluation lacks structured tools motivates the decision to build a reusable, extensible platform rather than a one-time script. 

Recommendations for future work are threefold. First, the platform should be extended to test servers that actively enforce OAuth or SMART-on-FHIR, enabling meaningful evaluation of the Authenticated Token Isolation scenario. Second, the Observation Bundle scenario should be extended with deduplication checks and search-based verification to produce more definitive results across server implementations. Third, the covert channel findings suggest that production FHIR deployments should consider whitelisting permitted extension URLs rather than accepting arbitrary ones. 

## **Reflection** 

## **Aspirations** 

When I accepted the traineeship at the University of Applied Sciences Upper Austria, I had two main goals. The first was technical: to deepen my knowledge of backend development, security testing, and REST API design in a real research environment. The second was personal: to grow as an independent professional. During my Erasmus semester through the Global Acting in IT minor, I had already visited Austria and got to know the university. That experience showed me how much I grow when I step outside my comfort zone. As a result, when the opportunity arose to return for a full traineeship, I took it without hesitation. 

## **Insights** 

Working entirely independently from the very beginning, from defining the plan and formulating the research question to implementing the platform and drawing conclusions, was both the biggest challenge and the most valuable aspect of this traineeship. There was no team to fall back on and no one to divide tasks with. Every decision was mine to make. 

On a technical level, I significantly improved my skills in Java and Spring Boot, Angular, and REST API security. I also gained practical insight into how security testing works in practice, not just executing probes, but critically evaluating the quality of results. A concrete example was the OAuth detection heuristic that initially produced false positives, classifying open sandbox servers as misconfigured. Discovering this problem and fixing it by replacing the keyword scan with strict CapabilityStatement parsing taught me that in security work, the interpretation of results is just as important as the results themselves. 

Another challenge was the limited availability of FHIR-specific security research. Because FHIR is a relatively new framework, there was little academic literature directly addressing the security evaluation of FHIR servers. This forced me to take a broader approach, grounding the research in general REST API security principles from OWASP and adapting them to the FHIR context. While this was frustrating at times, it also made the research more original and meaningful. 

The planning also taught me an important lesson. Towards the end of the project, accumulated fixes and refinements, particularly around the classification model, forced me to reprioritise features that I had originally planned. Looking back, I would have spent more time at the beginning learning the FHIR specification and creating a more realistic project plan with extra time for unexpected challenges. 

Working within an R&D department was a new experience for me. Participating in daily standups and seeing how research-driven development operates in practice gave me a much clearer picture of how professional teams work. My supervisor, Christoph Praschl, was always available when problems arose and made sure I genuinely understood his feedback rather than just receiving it. That kind of guidance made a real difference. 

On a personal level, living and working independently in Austria again reinforced something I first discovered during the Global Acting in IT minor: when I step outside my familiar environment, I become more confident and more adaptable. I now feel that I can navigate almost any professional or personal situation, whether it involves working in a foreign country, building something from 

scratch, or finding solutions without a safety net. That sense of self-reliance is something no classroom can fully teach. 

## **Moving forward** 

This traineeship made clear to me that international experience genuinely changes how you work and who you are. The combination of technical depth, research independence, and living abroad has made me more confident, more resilient, and more aware of my own strengths and areas for growth. 

Looking ahead, I want to invest more time in the planning phase of future projects, especially when working in unfamiliar technical domains. I also want to continue developing my ability to communicate findings clearly to non-technical stakeholders. This became particularly relevant when writing the research sections of this thesis. 

I am grateful for the opportunity to have completed this traineeship in Austria and to the colleagues in the R&D department for their support and openness. It has been one of the most formative experiences of my studies, and I leave with both stronger technical skills and a clearer sense of the kind of professional I want to become. 

## **Bibliography** 

- [1] M. Lindquist, “Interoperability in Healthcare Explained,” Oracle, 24 June 2024. [Online]. Available: https://www.oracle.com/health/interoperability-healthcare/. [Accessed 04 03 2026]. 

- [2]  M. K. Trubicki, “University of Applied Sciences Upper Austria,” [Online]. Available: https://www.wtu-n.net/university-of-applied-sciences-upper-austria/. [Accessed 04 03 2026]. 

- [3]  studyinaustria, “University of Applied Sciences Upper Austria,” [Online]. Available: https://studyinaustria.at/en/study/institutions/university-of-applied-sciences/university-ofapplied-sciences-upper-austria. [Accessed 09 03 2026]. 

- [4]  University of Applied Sciences Upper Austria, “Cooperation opportunities,” [Online]. Available: https://fh-ooe.at/en/cooperations. [Accessed 26 04 2026]. 

- [5]  HL7, “FHIR Overview,” [Online]. Available: https://www.hl7.org/fhir/overview.html. [Accessed 08 03 2026]. 

- [6]  OWASP Foundation, “OWASP API Security Top 10,” [Online]. Available: https://owasp.org/APISecurity/. [Accessed 08 03 2026]. 

- [7]  Approov Limited, “Playing with FHIR: Hacking and Securing FHIR API Implementations,” [Online]. Available: https://approov.io/hacking-and-securing-fhir-api-implementations. [Accessed 09 03 2026]. 

- [8]  Medblocks, “What are FHIR Resources? Explained,” [Online]. Available: https://medblocks.com/training/courses/fhir-fundamentals/fhir-fun-1-3-what-are-fhirresources. [Accessed 11 03 2026]. 

- [9]  HL7, “RESTful API,” 26 03 2023. [Online]. Available: https://www.hl7.org/fhir/http.html. [Accessed 08 03 2026]. 

- [10] OWASP Foundation, “OWASP Top 10 API Security Risks,” 2023. [Online]. Available: https://owasp.org/API-Security/editions/2023/en/0x11-t10/. [Accessed 31 03 2026]. 

- [11] M. Bek, “FHIR is transforming interoperability in healthcare – but what is it?,” 16 10 2025. [Online]. Available: https://fire.ly/blog/fhir-is-transforming-interoperability-in-healthcare-butwhat-is-it/. [Accessed 04 03 2026]. 

- [12] JMIR Publications, “The Fast Health Interoperability Resources (FHIR) Standard: Systematic Literature Review of Implementations, Applications, Challenges and Opportunities,” 30 07 2021. [Online]. Available: https://medinform.jmir.org/2021/7/e21929. [Accessed 01 04 2026]. 

- [13] HL7, “FHIR Security,” 26 03 2023. [Online]. Available: https://hl7.org/fhir/security.html. [Accessed 01 04 2026]. 

- [14] National Cyber Security Centre, “Securing HTTP-based APIs,” [Online]. Available: https://www.ncsc.gov.uk/collection/securing-http-based-apis/4-input-validation. [Accessed 26 04 2026]. 

- [15] HL7, “Validating Resources,” 26 03 2023. [Online]. Available: https://fhir.hl7.org/fhir/validation.html. [Accessed 01 04 2026]. 

- [16] L. McKenzie, “Public Test Servers,” [Online]. Available: https://confluence.hl7.org/spaces/FHIR/pages/35718859/Public+Test+Servers. [Accessed 29 04 2026]. 

## **Appendices** 

## **A. Description Appendix A** 

## Figure 2. Server Management View 

## Figure 3. Security Test Runner View 

## Figure 4. Security Test Results Overview 

## **B. Description Appendix B** 

The classification outcomes described below depend on whether the target server advertises OAuth or SMART support in its CapabilityStatement. On servers that do not declare authentication requirements, such as the three public sandboxes evaluated in this project, authentication and access control scenarios produce OPEN_POLICY outcomes rather than VULNERABLE or MISCONFIGURED. The scenario specifications below describe the full classification logic applicable to all server types. 

The following section provides a full specification of each attack scenario implemented in the platform. Scenarios are presented in the same four groups used during execution. 

## **Group 1: Input validation** 

The first group targets how FHIR servers parse and validate incoming request bodies. Three scenarios are included. 

**Malformed JSON Request** sends two structurally invalid payloads to POST /Patient: a truncated JSON body and a body with a trailing comma, both of which violate JSON syntax. A correctly implemented server is expected to reject these with a 4xx status code. If the server returns a 2xx response, a follow-up GET /Patient/{id} is issued to check whether a resource was actually created. A retrievable resource created from malformed input indicates a permissive or partial parser and is classified as VULNERABLE with HIGH severity. 

**Metadata Manipulation** probes semantic validation through three sub-probes sent to POST /Patient. The first submits a meta.versionId field with a numeric value instead of the required string type. The second sends a payload with an entirely fabricated resourceType value. The third embeds a null-byte character sequence inside the id field. A server that accepts a fabricated resource type is classified as VULNERABLE with HIGH severity. Echoing a client-supplied numeric versionId back in the response is treated as MEDIUM severity, since it indicates the server is not enforcing field type constraints. Nullbyte reflection in a subsequent GET is also classified as HIGH severity. 

**Unexpected Payload Injection** evaluates how servers handle fields that fall outside the FHIR schema. Three payloads are posted to POST /Patient: one containing unknown properties including a prototype pollution key, one with a duplicate id key, and one with arbitrarily named nested fields. A unique marker string is embedded in each payload. After a 2xx response, a follow-up GET is performed to check whether the marker was persisted and is retrievable. Persistence of injected markers is classified as VULNERABLE with MEDIUM severity, indicating over-permissive parsing. If a server accepts the request but strips unrecognised fields, the result is classified as SECURE. 

## **Group 2: Covert channel detection** 

The second group investigates whether FHIR servers can be used to store and retrieve hidden data by exploiting fields that are technically valid but semantically unusual. All three scenarios follow the same two-step method: a unique marker is submitted in a structurally valid payload, and a follow-up GET confirms whether it survived server-side storage. 

**Extension Fields Misuse** posts a Patient resource containing a custom FHIR extension with an arbitrary URL and a unique marker as its value. Extensions are a legitimate part of the FHIR specification, and servers are not required to reject them. The scenario, therefore, does not penalise acceptance alone. A follow-up GET is performed to determine whether the extension and its marker value were persisted and remain retrievable. If they are, the result is classified as VULNERABLE with MEDIUM severity, as the extension mechanism can be misused as a covert storage channel. 

**Contained Resource Smuggling** embeds a Binary resource inside the contained array of a Patient payload. The Binary resource carries a Base64-encoded marker string. After a 2xx response, a followup GET checks both the raw marker and its Base64 representation in the returned body. Persistence of the contained Binary and its payload is classified as VULNERABLE with MEDIUM severity. 

**Encoded Hidden Data** places a unique marker inside the meta.tag[].display field, encoding each character as a JSON unicode escape sequence. The use of unicode escaping is syntactically valid and should be normalised by a compliant server. A follow-up GET checks whether the decoded marker or the escaped form is present in the response. If so, the scenario is classified as VULNERABLE with LOW severity, reflecting that the meta.tag field can be used to encode arbitrary data that survives serverside storage. 

## **Group 3: Authentication** 

The third group examines how servers respond to requests that carry invalid, malformed, or entirely absent credentials. 

**Invalid Credentials Access Test** consolidates four authentication sub-probes. First, it attempts to discover a token endpoint by parsing /.well known/smart configuration and /metadata. If found, it submits a client_credentials grant request with an invalid client identifier; if the server issues an access token in response, this is classified as VULNERABLE with CRITICAL severity. Second, a GET /Patient request is issued with an invalid HTTP Basic credential header. Third, the same endpoint is queried with a syntactically valid but forged JWT bearer token carrying an invalid signature. Fourth, a series of malformed bearer tokens, including an empty bearer value, a non-JWT string, and an expired shaped token, are tested. For each read probe, the classification depends on whether OAuth is advertised in the server metadata. A 2xx response from a server that does advertise OAuth is classified as VULNERABLE; a 2xx response from a server that does not advertise OAuth is classified as OPEN_POLICY, reflecting that the server may intentionally be a public demo instance. 

**Open Endpoint Detection** compares what a server advertises against what it actually enforces. The scenario retrieves both /.well-known/smart-configuration and /metadata and checks whether either document declares OAuth or SMART support in the formal rest[].security field. It then issues an unauthenticated GET /Patient?\_count=1. If OAuth is not advertised and the read succeeds, the result is OPEN\_POLICY, which is recorded separately from a genuine vulnerability to avoid penalising intentionally public servers. 

**Authenticated Token Isolation** is an optional scenario that requires a bearer token and an out-ofscope patient ID to be configured via environment variables. When configured, it issues a GET /Patient/{outOfScopeId} request with the provided token to test whether the server enforces tokenlevel patient isolation. A 200 response is classified as VULNERABLE with CRITICAL severity if OAuth is advertised, or HIGH severity if it is not. A 401, 403, or 404 response is classified as SECURE. If the required configuration is absent, the scenario returns INCONCLUSIVE. 

## **Group 4: Access control** 

The fourth group tests whether servers enforce isolation between different patients' data and whether unauthenticated write operations are permitted. 

**Cross-Patient Access** creates two Patient resources representing a victim and an attacker, then attempts to read the victim's Patient record directly via GET /Patient/{victimId}. It also creates an Observation linked to the victim and attempts to retrieve it via GET /Observation?subject=Patient/{victimId}. The outcome of each read is evaluated against whether OAuth is advertised in the server's CapabilityStatement. A successful unauthenticated read on a server that advertises OAuth is classified as VULNERABLE; the same read on a server without advertised OAuth is classified as OPEN_POLICY 

**Unauthorised Write / ID Tampering** runs two sub-probes. The first creates a victim Patient and then issues an unauthenticated PUT /Patient/{id} with a modified name field containing a unique marker. A follow-up GET verifies whether the modification persisted. If it did and OAuth is advertised, the result is VULNERABLE; if no OAuth is advertised, the result is OPEN\_POLICY. If the PUT succeeded but no change is observable in the subsequent GET, the result is INCONCLUSIVE to avoid overclaiming. The second sub-probe creates an Observation referencing the victim's ID and issues a follow-up GET to confirm whether it is accessible. Classification in both sub-probes follows the same advertised-vs-observed OAuth heuristic used across the authentication and access control groups. 

**Observation Bundle / Duplicate Clinical** submits a FHIR transaction Bundle containing three POST Observation entries for the same patient, each using LOINC code 718-7. The scenario verifies whether all three entries are created by counting 201 responses in the Bundle reply. If three or more entries are created and OAuth is advertised, the result is VULNERABLE with MEDIUM severity. If no OAuth is advertised, the result is OPEN_POLICY. Partial creation or an unexpected status code results in INCONCLUSIVE. 

