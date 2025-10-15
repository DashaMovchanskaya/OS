#include "marker_logic.h"

CRITICAL_SECTION csT;
int* arrT;
int sizeT = 10;

HANDLE startEvent;
HANDLE stopEvent;
HANDLE exitEvent;
HANDLE threadHandle;

bool tryMark(int* arr, int size, int index, int threadId) {
    if (index < 0 || index >= size) return false;
    if (arr[index] != 0) return false;
    arr[index] = threadId;
    return true;
}

void resetMarks(std::vector<int>& indices, int* arr, int size) {
    for (int i : indices) {
        if (i >= 0 && i < size) arr[i] = 0;
    }
}

bool allFinished(const std::vector<bool>& flags) {
    for (bool f : flags) if (!f) return false;
    return true;
}


DWORD WINAPI testMarker(LPVOID param) {
    int threadIndex = reinterpret_cast<std::intptr_t>(param);
    std::vector<int> marked;

    WaitForSingleObject(startEvent, INFINITE);
    srand(threadIndex + 1);

    for (int i = 0; i < 5; ++i) {
        EnterCriticalSection(&csT);
        int idx = rand() % sizeT;
        if (arrT[idx] == 0) {
            arrT[idx] = threadIndex + 1;
            marked.push_back(idx);
        }
        LeaveCriticalSection(&csT);
        Sleep(10);
    }

    SetEvent(stopEvent);
    WaitForSingleObject(exitEvent, INFINITE);

    EnterCriticalSection(&csT);
    for (int i : marked) {
        arrT[i] = 0;
    }
    LeaveCriticalSection(&csT);

    return 0;
}