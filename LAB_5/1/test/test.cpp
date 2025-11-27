#include "pch.h"
#include <windows.h>
#include <thread>
#include <vector>
#include <atomic>
#include <chrono>
#include <iostream>
#include <fstream>
#include <map>
#include <cstring>

struct Employee {
    int num;
    char name[10];
    double hours;
};

int employeesCount; 
int clientsCount;
std::string fileName;
std::vector<Employee> employees;

std::map<int, HANDLE> test_semaphores;
std::vector<Employee> test_employees = {
    {1001, "Alice", 40.0},
    {1002, "Bob", 35.5},
    {1003, "Charlie", 50.0}
};
HANDLE test_mutex;
int test_clientsCount = 5; 

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
    if (!outputFile) { std::cerr << "Cannot open file for writing.\n"; return; }
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

Employee test_readEmployeeByID(int id) {
    for (const auto& e : test_employees) {
        if (e.num == id) return e;
    }
    return { 0, "", 0.0 };
}

void test_modifyEmployee(const Employee& emp) {
    for (auto& e : test_employees) {
        if (e.num == emp.num) {
            std::memcpy(&e, &emp, sizeof(Employee));
            break;
        }
    }
}

void locked_read(int id) {
    HANDLE semaphore = test_semaphores[id];
    WaitForSingleObject(semaphore, INFINITE);
    test_readEmployeeByID(id);
    ReleaseSemaphore(semaphore, 1, NULL);
}

void locked_write(const Employee& emp) {
    HANDLE semaphore = test_semaphores[emp.num];
    WaitForSingleObject(test_mutex, INFINITE);
    for (int i = 0; i < test_clientsCount; ++i) {
        WaitForSingleObject(semaphore, INFINITE);
    }
    ReleaseMutex(test_mutex);
    test_modifyEmployee(emp);
    ReleaseSemaphore(semaphore, test_clientsCount, NULL);
}

void init_test_sync() {
    test_mutex = CreateMutex(NULL, FALSE, NULL);
    for (const auto& e : test_employees) {
        test_semaphores[e.num] = CreateSemaphore(NULL, test_clientsCount, test_clientsCount, NULL);
    }
}

void cleanup_test_sync() {
    CloseHandle(test_mutex);
    for (auto& pair : test_semaphores) {
        CloseHandle(pair.second);
    }
    test_semaphores.clear();
}

TEST(Synchronization, MultipleReaders_NoBlock) {
    init_test_sync();
    std::atomic<int> completed{ 0 };
    auto start = std::chrono::high_resolution_clock::now();

    auto reader = [&completed](int id) {
        locked_read(id);
        std::this_thread::sleep_for(std::chrono::milliseconds(100));
        completed++;
        };

    std::vector<std::thread> threads;
    for (int i = 0; i < 5; ++i) {
        threads.emplace_back(reader, 1001);
    }
    for (auto& t : threads) t.join();

    auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(
        std::chrono::high_resolution_clock::now() - start).count();

    EXPECT_EQ(completed.load(), 5);
    EXPECT_LT(duration, 400);

    cleanup_test_sync();
}

TEST(Synchronization, WriterBlockedByReaders) {
    init_test_sync();
    std::atomic<bool> readers_holding{ false };
    std::atomic<bool> writer_passed{ false };

    std::vector<std::thread> readers;
    for (int i = 0; i < test_clientsCount; ++i) {
        readers.emplace_back([&readers_holding](int id) {
            HANDLE semaphore = test_semaphores[id];
            WaitForSingleObject(semaphore, INFINITE);
            readers_holding = true;  
            std::this_thread::sleep_for(std::chrono::milliseconds(500));
            ReleaseSemaphore(semaphore, 1, NULL);
            }, 1001);
    }

    while (!readers_holding) std::this_thread::sleep_for(std::chrono::milliseconds(10));

    std::thread writer([&writer_passed](int id) {
        locked_write({ id, "David", 99.9 });
        writer_passed = true;
        }, 1001);

    std::this_thread::sleep_for(std::chrono::milliseconds(200));
    EXPECT_FALSE(writer_passed); 

    for (auto& t : readers) t.join();
    writer.join();

    EXPECT_TRUE(writer_passed);
    EXPECT_STREQ(test_readEmployeeByID(1001).name, "David");

    cleanup_test_sync();
}

TEST(Synchronization, WriterBlocksReaders) {
    init_test_sync();
    std::atomic<bool> writer_holding{ false };

    std::thread writer([&writer_holding](int id) {
        HANDLE semaphore = test_semaphores[id];
        WaitForSingleObject(test_mutex, INFINITE);
        for (int i = 0; i < test_clientsCount; ++i) {
            WaitForSingleObject(semaphore, INFINITE);
        }
        ReleaseMutex(test_mutex);
        writer_holding = true;
        test_modifyEmployee({ id, "Eve", 30.0 });
        std::this_thread::sleep_for(std::chrono::milliseconds(400));
        ReleaseSemaphore(semaphore, test_clientsCount, NULL);
        }, 1002);

    while (!writer_holding) std::this_thread::sleep_for(std::chrono::milliseconds(10));

    std::atomic<int> blocked_count{ 0 };
    auto reader_func = [&blocked_count](int id) {
        auto start = std::chrono::high_resolution_clock::now();
        locked_read(id);
        auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(
            std::chrono::high_resolution_clock::now() - start).count();
        if (duration > 200) blocked_count++;
        };

    std::vector<std::thread> readers;
    for (int i = 0; i < 3; ++i) {
        readers.emplace_back(reader_func, 1002);
    }
    for (auto& t : readers) t.join();
    writer.join();

    EXPECT_GT(blocked_count.load(), 0);
    EXPECT_STREQ(test_readEmployeeByID(1002).name, "Eve");

    cleanup_test_sync();
}

TEST(Synchronization, NoDeadlock_MultipleWriters) {
    init_test_sync();
    std::atomic<int> writers_completed{ 0 };

    auto writer_func = [&writers_completed](int id, const char* name, double hours) {
        locked_write({ id, "", hours });
        std::strncpy(test_readEmployeeByID(id).name, name, 9);
        writers_completed++;
        };

    std::vector<std::thread> writers;
    writers.emplace_back(writer_func, 1003, "Frank", 60.0);
    std::this_thread::sleep_for(std::chrono::milliseconds(50));
    writers.emplace_back(writer_func, 1003, "Grace", 55.5);
    writers.emplace_back(writer_func, 1003, "Henry", 70.0);

    for (auto& t : writers) t.join();

    EXPECT_EQ(writers_completed.load(), 3);

    cleanup_test_sync();
}

TEST(FileOperations, CreateAndDisplayEmployees) {
    std::string test_file = "test.bin";
    fileName = test_file;
    employees.clear();
    employeesCount = 2;
    employees.push_back({ 1, "Test1", 10.0 });
    employees.push_back({ 2, "Test2", 20.0 });

    std::ofstream out(test_file, std::ios::binary);
    for (const auto& e : employees) {
        out.write(reinterpret_cast<const char*>(&e), sizeof(e));
    }
    out.close();

    testing::internal::CaptureStdout();
    displayEmployees();
    std::string output = testing::internal::GetCapturedStdout();

    EXPECT_TRUE(output.find("ID: 1, Name: Test1, Hours: 10") != std::string::npos);
    EXPECT_TRUE(output.find("ID: 2, Name: Test2, Hours: 20") != std::string::npos);

    std::remove(test_file.c_str());
}

TEST(FileOperations, ModifyEmployee) {
    std::string test_file = "test.bin";
    fileName = test_file;
    Employee original = { 1, "Old", 10.0 };
    std::ofstream out(test_file, std::ios::binary);
    out.write(reinterpret_cast<const char*>(&original), sizeof(original));
    out.close();

    Employee modified = { 1, "New", 20.0 };
    modifyEmployee(modified);

    Employee read = readEmployeeByID(1);
    EXPECT_STREQ(read.name, "New");
    EXPECT_DOUBLE_EQ(read.hours, 20.0);

    std::remove(test_file.c_str());
}

TEST(NamedPipe, SmokeTestCommunication) {
    const wchar_t* TEST_PIPE = L"\\\\.\\pipe\\test_employee_pipe";

    std::atomic<bool> server_ready{ false };
    std::thread server([&]() {
        HANDLE hPipe = CreateNamedPipeW(
            TEST_PIPE,
            PIPE_ACCESS_DUPLEX,
            PIPE_TYPE_MESSAGE | PIPE_READMODE_MESSAGE | PIPE_WAIT,
            1, 4096, 4096, 0, NULL
        );
        ASSERT_NE(hPipe, INVALID_HANDLE_VALUE);
        server_ready = true;

        if (ConnectNamedPipe(hPipe, NULL) || GetLastError() == ERROR_PIPE_CONNECTED) {
            int choice, id;
            DWORD dw;
            ReadFile(hPipe, &choice, sizeof(choice), &dw, NULL);
            ReadFile(hPipe, &id, sizeof(id), &dw, NULL);

            Employee emp = { id, "Test", 42.0 };
            WriteFile(hPipe, &emp, sizeof(emp), &dw, NULL);

            char ack;
            ReadFile(hPipe, &ack, 1, &dw, NULL);
        }
        DisconnectNamedPipe(hPipe);
        CloseHandle(hPipe);
        });

    while (!server_ready) std::this_thread::sleep_for(std::chrono::milliseconds(10));

    HANDLE hClient = CreateFileW(TEST_PIPE, GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, 0, NULL);
    ASSERT_NE(hClient, INVALID_HANDLE_VALUE);

    int choice = 2, id = 999;
    DWORD dw;
    WriteFile(hClient, &choice, sizeof(choice), &dw, NULL);
    WriteFile(hClient, &id, sizeof(id), &dw, NULL);

    Employee response{};
    ASSERT_TRUE(ReadFile(hClient, &response, sizeof(response), &dw, NULL));
    EXPECT_EQ(dw, sizeof(Employee));
    EXPECT_EQ(response.num, 999);
    EXPECT_STREQ(response.name, "Test");

    char ack = 1;
    WriteFile(hClient, &ack, 1, &dw, NULL);

    CloseHandle(hClient);
    server.join();
}

int main(int argc, char** argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}