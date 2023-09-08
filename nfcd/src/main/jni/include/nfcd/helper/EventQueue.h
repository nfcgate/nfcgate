#ifndef NFCD_EVENTQUEUE_H
#define NFCD_EVENTQUEUE_H

#include <stack>
#include <mutex>
#include <condition_variable>

class EventQueue {
    struct BoolGuard {
        // set flag on construction
        explicit BoolGuard(bool &var) : mVar(var = true) { }
        // reset flag on destruction
        ~BoolGuard() {
            mVar = false;
        }

        bool &mVar;
    };

    // every event has an event and a status code
    struct EventEntry {
        uint8_t event, status;
    };

public:
    // Start collecting incoming events into the queue
    void beginCollecting() {
        std::lock_guard<std::mutex> lock(mMutex);
        mCollecting = true;
    }

    // Insert event and status into event queue.
    // If another thread is waiting for this event, the wait will end in success.
    void enqueue(uint8_t event, uint8_t status) {
        std::unique_lock<std::mutex> lock(mMutex);
        if (mCollecting) {
            mQueue.push({event, status});
            lock.unlock();

            mCond.notify_one();
        }
    }

    // Wait for specified time for given event to arrive, resulting in status.
    // Returns true if the event arrived, false if timeout was reached.
    bool waitFor(uint8_t event, uint8_t &status, uint64_t millis) {
        std::unique_lock<std::mutex> lock(mMutex);
        // ensure collecting is set to false on exiting scope, even if wait_for throws an exception
        BoolGuard guard(mCollecting);

        return mCond.wait_for(lock, std::chrono::milliseconds(millis), [&] () {
            return find(event, status);
        });
    }

protected:
    // Returns true if specified event was found in the queue. Pops all other events along the way.
    bool find(uint8_t event, uint8_t &status) {
        for (; !mQueue.empty(); mQueue.pop()) {
            auto &entry = mQueue.top();

            if (entry.event == event) {
                status = entry.status;
                return true;
            }
        }

        return false;
    }

    // synchronization members
    mutable std::mutex mMutex;
    std::condition_variable mCond;
    bool mCollecting = false;

    // queue backed by std::deque
    std::stack<EventEntry> mQueue;
};

#endif //NFCD_EVENTQUEUE_H
