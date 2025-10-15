// marker_thread.cpp
#include "marker_logic.h"

int sizeT = 0;
int* arrT = nullptr;
std::mutex arrMutexT;
std::mutex coutMutexT;

std::condition_variable startCVT;
std::mutex startMutexT;
bool startSignalT = false;

std::vector<std::unique_ptr<std::mutex>> threadMutexesT;
std::vector<std::unique_ptr<std::condition_variable>> threadCVsT;

std::vector<bool> threadSignalsT;
std::vector<bool> threadTerminateT;
std::vector<bool> threadsFinishedT;

std::condition_variable allSignaledCVT;
std::mutex allSignaledMutexT;


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


void testMarker(int threadIndex) {
    std::vector<int> markedIndices;

    {
        std::unique_lock<std::mutex> lock(startMutexT);
        startCVT.wait(lock, [] { return startSignalT; });
    }

    srand(threadIndex + 1);

    for (int i = 0; i < 5; ++i) {
        int randomIndex = rand() % sizeT;

        {
            std::lock_guard<std::mutex> lock(arrMutexT);
            if (arrT[randomIndex] == 0) {
                arrT[randomIndex] = threadIndex + 1;
                markedIndices.push_back(randomIndex);
            }
        }

        std::this_thread::sleep_for(std::chrono::milliseconds(10));
    }

    {
        std::lock_guard<std::mutex> lock(*threadMutexesT[threadIndex]);
        threadSignalsT[threadIndex] = true;
    }
    allSignaledCVT.notify_one();

    {
        std::unique_lock<std::mutex> lock(*threadMutexesT[threadIndex]);
        threadCVsT[threadIndex]->wait(lock, [&] { return threadTerminateT[threadIndex]; });
    }

    {
        std::lock_guard<std::mutex> lock(arrMutexT);
        for (int idx : markedIndices) {
            if (idx >= 0 && idx < sizeT) {
                arrT[idx] = 0;
            }
        }
    }

    {
        std::lock_guard<std::mutex> lock(allSignaledMutexT);
        threadsFinishedT[threadIndex] = true;
    }
}
