#include <windows.h>
#include <iostream>
#include <vector>

CRITICAL_SECTION cs;
int* arr;
int size;

HANDLE* threadHandles;
HANDLE* startEvents;
HANDLE* stopEvents;
HANDLE* exitEvents;

DWORD WINAPI marker(LPVOID param) {
    int threadIndex = reinterpret_cast<std::intptr_t>(param);
    int markedCount = 0;
    std::vector<int> markedIndices;

    WaitForSingleObject(startEvents[threadIndex], INFINITE);
    
    srand(threadIndex + 1);

    while (true) {
        EnterCriticalSection(&cs);
    
        int randomNumber = rand() % size;
        
        if (arr[randomNumber] == 0) {
            Sleep(5);
            arr[randomNumber] = threadIndex + 1;
            markedIndices.push_back(randomNumber);
            markedCount++;
            Sleep(5);
            LeaveCriticalSection(&cs);
        }
        else {
            std::cout << "Thread: " << threadIndex << "\n";
            std::cout << "Number of marked elements: " << markedCount << "\n";
            std::cout << "Element index that cannot be marked: " << randomNumber << "\n";

            LeaveCriticalSection(&cs);

            SetEvent(stopEvents[threadIndex]);
            ResetEvent(startEvents[threadIndex]);

            HANDLE events[2] = { startEvents[threadIndex], exitEvents[threadIndex] };
            DWORD result = WaitForMultipleObjects(2, events, FALSE, INFINITE);

            if (result == WAIT_OBJECT_0 + 1) {
                EnterCriticalSection(&cs);
                for (int i : markedIndices) {
                    arr[i] = 0;
                }
                LeaveCriticalSection(&cs);
                return 0;
            }
            else {
                ResetEvent(stopEvents[threadIndex]); 
                continue;
            }
        }
    }
}

int main() {
    std::cout << "Enter size of array: ";
    std::cin >> size;
    arr = new int[size]();


    int countOfThreads = 0;
    std::cout << "Enter count of marker call: ";
    std::cin >> countOfThreads;

    InitializeCriticalSection(&cs);

    threadHandles = new HANDLE[countOfThreads];
    startEvents = new HANDLE[countOfThreads];
    stopEvents = new HANDLE[countOfThreads];
    exitEvents = new HANDLE[countOfThreads];

    for (int i = 0; i < countOfThreads; i++) { 
        startEvents[i] = CreateEvent(NULL, TRUE, FALSE, NULL);
        stopEvents[i] = CreateEvent(NULL, TRUE, FALSE, NULL);
        exitEvents[i] = CreateEvent(NULL, TRUE, FALSE, NULL);
        threadHandles[i] = CreateThread(NULL, 0, marker, reinterpret_cast<LPVOID>(static_cast<INT_PTR>(i)), 0, NULL); 
    }

    for (int i = 0; i < countOfThreads; i++) {
        SetEvent(startEvents[i]); 
    }
        
    bool* finished = new bool[countOfThreads]();
    int completed = 0;

    while (completed < countOfThreads) {
        WaitForMultipleObjects(countOfThreads, stopEvents, TRUE, INFINITE);

        EnterCriticalSection(&cs);
     
        std::cout << "Array content: ";
        for (int i = 0; i < size; i++) {
            std::cout << arr[i] << " ";
        }
        std::cout << "\n";
        LeaveCriticalSection(&cs);

        int stopIndex;
        std::cout << "Enter the serial number of the thread to stop its work: ";
        std::cin >> stopIndex;
        stopIndex--;

        if (stopIndex < 0 || stopIndex >= countOfThreads || finished[stopIndex]) {
            std::cout << "Invalid thread number!\n";
            continue;
        }

        finished[stopIndex] = true;
        completed++;

        SetEvent(exitEvents[stopIndex]);
        WaitForSingleObject(threadHandles[stopIndex], INFINITE);

        CloseHandle(threadHandles[stopIndex]);
        CloseHandle(exitEvents[stopIndex]);
        CloseHandle(startEvents[stopIndex]);

        EnterCriticalSection(&cs);
        std::cout << "Final array content: ";
        for (int i = 0; i < size; i++) {
            std::cout << arr[i] << " ";
        }
        std::cout << "\n";
        LeaveCriticalSection(&cs);

        for (int i = 0; i < countOfThreads; i++) {
            if (!finished[i]) {
                ResetEvent(stopEvents[i]);
                SetEvent(startEvents[i]);
            }
        }
    }

    delete[] arr;
    delete[] threadHandles;
    delete[] startEvents;
    delete[] stopEvents;
    delete[] exitEvents;
    delete[] finished;
    DeleteCriticalSection(&cs);

    std::cout << "All threads finished. Program completed." << std::endl; 
    return 0;
}
