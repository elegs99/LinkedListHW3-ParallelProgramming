# LinkedListHW3-ParallelProgramming

Problem 1:<br>
The reason there would be presents left in the bag is because we are alternating between adding and removing presents from the list. Although this saves us from having to handle a long linked list it will often lead to the giftbag being empty while other threads are still running and adding gifts. This means the servants/threads will stop looking for gifts to remove from the list even though other threads may be in the process of still adding them. 

The solution I came up with for this problem is to have another check for if the list is empty once the threads are completely finished adding. This ensures that any gifts left over will always be handled even if the threads are held up for a long time.

<br><br>
Problem 2:<br>
This program runs very fast as it is just adding 480 values to the linked list then printing those values. There is guarenteed to be progress as I am using the lock free list implmentation that was shown to us in chapter 9. Using this type of fine grain locking I can ensure there will be no deadlock even when all threads are adding to the list simultaneously.
