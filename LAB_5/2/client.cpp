#include <iostream>
#include <iomanip>
#include <string>
#include <conio.h>
#include <boost/asio.hpp>

namespace asio = boost::asio;
using boost::system::error_code;

struct Employee {
    int num;
    char name[10];
    double hours;
};

int main() {
    try {
        asio::io_context io_context;
        asio::ip::tcp::socket socket(io_context);

        std::cout << "Connecting to server..." << std::endl;
        asio::ip::tcp::resolver resolver(io_context);
        asio::ip::tcp::resolver::results_type endpoints = resolver.resolve("127.0.0.1", "12345");
        asio::connect(socket, endpoints);
        std::cout << "Connected to server successfully!" << std::endl;

        bool exit = false;
        while (!exit) {
            int choice;
            std::cout << "Enter number of operation:\n1 - modification of file recording;\n2 - reading of the record;\n3 - exit from the cycle;" << std::endl;
            std::cin >> choice;
            int ID;

            switch (choice) {
            case 1: {
                std::cout << "Enter employee's ID to modify file." << std::endl;
                std::cin >> ID;

                asio::write(socket, asio::buffer(&choice, sizeof(choice)));
                asio::write(socket, asio::buffer(&ID, sizeof(ID)));

                Employee employee;
                asio::read(socket, asio::buffer(&employee, sizeof(employee)));

                if (employee.num != 0) {
                    std::cout << "ID: " << employee.num << ", Name: " << employee.name << ", Hours: " << employee.hours << std::endl;
                }
                else {
                    std::cout << "Employee with ID " << ID << " isn't found." << std::endl;
                    std::cout << "Press any key to continue." << std::endl;
                    _getch(); 
                    char c = 1;
                    asio::write(socket, asio::buffer(&c, sizeof(c)));
                    break;
                }

                std::cout << "Enter new name (max 9 symbols): ";
                std::cin >> std::setw(10) >> employee.name;
                employee.name[9] = '\0';

                std::cout << "Enter hours: ";
                std::cin >> employee.hours;
                employee.num = ID;

                asio::write(socket, asio::buffer(&employee, sizeof(employee)));

                std::cout << "Press Enter to push information to server..." << std::endl;
                std::cin.ignore();
                std::cin.get();

                std::cout << "Data for modification are written." << std::endl;
                std::cout << "Press any key to continue." << std::endl;
                _getch();
                char c = 1;
                asio::write(socket, asio::buffer(&c, sizeof(c)));
                break;
            }
            case 2: {
                std::cout << "Enter employee's ID to read of the record." << std::endl;
                std::cin >> ID;

                asio::write(socket, asio::buffer(&choice, sizeof(choice)));
                asio::write(socket, asio::buffer(&ID, sizeof(ID)));

                Employee employee{};
                asio::read(socket, asio::buffer(&employee, sizeof(employee)));

                if (employee.num != 0) {
                    std::cout << "ID: " << employee.num << ", Name: " << employee.name << ", Hours: " << employee.hours << std::endl;
                }
                else {
                    std::cout << "Employee with ID " << ID << " isn't found." << std::endl;
                }

                std::cout << "Press any key to continue." << std::endl;
                _getch(); 
                char c = 1;
                asio::write(socket, asio::buffer(&c, sizeof(c)));
                break;
            }
            case 3: {
                exit = true;
                break;
            }
            default: {
                std::cout << "Enter correct number, please." << std::endl;
                break;
            }
            }
        }

        socket.close();
        std::cout << "Client finished." << std::endl;
    }
    catch (const std::exception& e) {
        std::cerr << "Client error: " << e.what() << std::endl;
        std::cout << "Press Enter to exit..." << std::endl;
        std::cin.ignore();
        std::cin.get();
        return 1;
    }

    return 0;
}