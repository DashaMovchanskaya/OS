#include <iostream>
#include <iomanip>
#include <fstream>
#include <string>
#include <vector>
#include <map>
#include <thread>
#include <mutex>
#include <condition_variable>

#include <boost/interprocess/sync/named_mutex.hpp>
#include <boost/interprocess/sync/named_semaphore.hpp>
#include <boost/interprocess/sync/scoped_lock.hpp>
#include <boost/asio.hpp>

namespace bip = boost::interprocess;
namespace asio = boost::asio;
using boost::system::error_code;

struct Employee {
    int num;
    char name[10];
    double hours;
};

const std::string PIPE_NAME = "employee_pipe";
int employeesCount;
int clientsCount;
std::string fileName;
std::map<int, bip::named_semaphore*> semaphores;
std::vector<Employee> employees;

bip::named_mutex* global_mutex;

void createFile() {
    std::ofstream file(fileName, std::ios::binary);
    if (!file) {
        std::cerr << "The file could not be opened." << std::endl;
        return;
    }

    employeesCount = 0;
    std::cout << "Enter count of employees: ";
    std::cin >> employeesCount;

    for (int i = 0; i < employeesCount; ++i) {
        Employee employee;
        std::cout << "Enter information about employee number " << (i + 1) << ": ";
        std::cout << "Enter identification number: ";
        std::cin >> employee.num;
        std::cout << "Enter name: ";
        std::cin >> std::setw(10) >> employee.name;
        employee.name[9] = '\0';
        std::cout << "Enter hours: ";
        std::cin >> employee.hours;
        employees.push_back(employee);
        file.write(reinterpret_cast<char*>(&employee), sizeof(employee));
    }
    file.close();
    std::cout << "Information about employees is written in binary file." << std::endl;
}

void displayEmployees() {
    std::ifstream file(fileName, std::ios::binary);
    if (!file) {
        std::cerr << "The file could not be opened." << std::endl;
        return;
    }

    std::cout << "File contents" << std::endl;
    Employee emp{};
    while (file.read(reinterpret_cast<char*>(&emp), sizeof(emp))) {
        std::cout << "ID: " << emp.num << ", Name: " << emp.name
            << ", Hours: " << emp.hours << std::endl;
    }
    std::cout << std::endl;
}

void modifyEmployee(const Employee& emp) {
    std::fstream outputFile(fileName, std::ios::binary | std::ios::in | std::ios::out);
    if (!outputFile) {
        std::cerr << "Cannot open file for writing.\n";
        return;
    }

    Employee temp;
    while (outputFile.read(reinterpret_cast<char*>(&temp), sizeof(temp))) {
        if (temp.num == emp.num) {
            outputFile.seekp(-static_cast<int>(sizeof(temp)), std::ios::cur);
            outputFile.write(reinterpret_cast<const char*>(&emp), sizeof(emp));
            break;
        }
    }
}

Employee readEmployeeByID(int employeeID) {
    std::ifstream inputFile(fileName, std::ios::binary);
    if (!inputFile) {
        std::cerr << "Cannot open file for reading." << std::endl;
        return { 0, "", 0.0 };
    }

    Employee emp{};
    while (inputFile.read(reinterpret_cast<char*>(&emp), sizeof(emp))) {
        if (emp.num == employeeID) {
            return emp;
        }
    }

    return { 0, "", 0.0 };
}

void processClient(asio::ip::tcp::socket socket) {
    try {
        asio::streambuf buffer;

        while (true) {
            int choice;
            error_code ec;
            size_t len = asio::read(socket, asio::buffer(&choice, sizeof(choice)), ec);

            if (ec || len == 0) break;

            int ID;
            len = asio::read(socket, asio::buffer(&ID, sizeof(ID)), ec);
            if (ec || len == 0) break;

            if (semaphores.find(ID) == semaphores.end()) {
                std::cerr << "Invalid ID: " << ID << " (no such employee)" << std::endl;
                Employee emptyEmp = { 0, "", 0.0 };
                asio::write(socket, asio::buffer(&emptyEmp, sizeof(emptyEmp)));
                char ack;
                asio::write(socket, asio::buffer(&ack, sizeof(ack)));
                continue;
            }

            bip::named_semaphore* semaphore = semaphores[ID];
            std::cout << "Request: choice=" << choice << ", ID=" << ID << std::endl;

            if (choice == 1) {
                char m;

                bip::scoped_lock<bip::named_mutex> lock(*global_mutex);

                for (int i = 0; i < clientsCount; i++) {
                    semaphore->wait();
                }

                Employee employee = readEmployeeByID(ID);
                asio::write(socket, asio::buffer(&employee, sizeof(employee)));

                asio::read(socket, asio::buffer(&employee, sizeof(employee)));
                modifyEmployee(employee);

                asio::read(socket, asio::buffer(&m, sizeof(m)));

                for (int i = 0; i < clientsCount; i++) { 
                    semaphore->post(); 
                }

            }
            else if (choice == 2) {
                char m;

                semaphore->wait();

                Employee emp = readEmployeeByID(ID);
                asio::write(socket, asio::buffer(&emp, sizeof(emp)));

                asio::read(socket, asio::buffer(&m, sizeof(m)));

                semaphore->post();
            }
        }
    }
    catch (const std::exception& e) {
        std::cerr << "Client processing error: " << e.what() << std::endl;
    }
}

int main() {
    try {
        std::cout << "Enter binary file name: ";
        std::getline(std::cin, fileName);

        createFile();
        displayEmployees();

        std::cout << "Enter count of Clients: ";
        std::cin >> clientsCount;

        bip::named_mutex::remove("global_mutex");
        global_mutex = new bip::named_mutex(bip::create_only, "global_mutex");

        for (const auto& emp : employees) {
            std::string sem_name = "emp_sem_" + std::to_string(emp.num);
            bip::named_semaphore::remove(sem_name.c_str());
            semaphores[emp.num] = new bip::named_semaphore(bip::create_only, sem_name.c_str(), clientsCount);
        }

        asio::io_context io_context;
        short port = 12345;
        asio::ip::tcp::acceptor acceptor(io_context, asio::ip::tcp::endpoint(asio::ip::tcp::v4(), port));

        std::cout << "Server starting on port " << port << "..." << std::endl;

        std::thread server_thread([&]() {
            try {
                while (true) {
                    auto socket = std::make_shared<asio::ip::tcp::socket>(io_context);
                    std::cout << "Waiting for client connection..." << std::endl;
                    acceptor.accept(*socket);
                    std::cout << "New client connected successfully!" << std::endl;
                    std::thread(processClient, std::move(*socket)).detach();
                }
            }
            catch (const std::exception& e) {
                std::cerr << "Server thread error: " << e.what() << std::endl;
            }
            });

        for (int i = 0; i < clientsCount; ++i) {
            std::string command = "start \"Client " + std::to_string(i + 1) + "\" Client.exe " + std::to_string(i + 1);
            std::cout << "Starting client " << (i + 1) << "..." << std::endl;

            int result = system(command.c_str());
            if (result != 0) {
                std::cerr << "Failed to start client " << (i + 1) << ". Error code: " << result << std::endl;
            }
        }

        io_context.stop();
        server_thread.join();

        delete global_mutex;
        bip::named_mutex::remove("global_mutex");

        for (auto& pair : semaphores) {
            delete pair.second;
            std::string sem_name = "emp_sem_" + std::to_string(pair.first);
            bip::named_semaphore::remove(sem_name.c_str());
        }

        displayEmployees();
        std::cout << "Press Enter to exit server..." << std::endl;
        std::cin.ignore();
        std::cin.get();

        return 0;
    }
    catch (const std::exception& e) {
        std::cerr << "Server error: " << e.what() << std::endl;
        return 1;
    }
}