package main

import (
	"fmt"
	"log"
	"os"
	"sync"
	"time"
)

type Task struct {
	ID int
}

func (t Task) Run() string {
	time.Sleep(100 * time.Millisecond) // Simulated work
	return fmt.Sprintf("Task %d processed successfully.", t.ID)
}

func worker(id int, taskChan <-chan Task, results *[]string, mu *sync.Mutex, wg *sync.WaitGroup) {
	defer wg.Done()
	log.Printf("Worker %d started.\n", id)

	for task := range taskChan {
		result := task.Run()

		// Lock results
		mu.Lock()
		*results = append(*results, result)
		mu.Unlock()
	}

	log.Printf("Worker %d finished.\n", id)
}

func main() {
	tasks := make(chan Task, 10)
	var results []string
	var mu sync.Mutex
	var wg sync.WaitGroup

	for i := 1; i <= 10; i++ {
		tasks <- Task{ID: i}
	}
	close(tasks)

	for i := 1; i <= 3; i++ {
		wg.Add(1)
		go worker(i, tasks, &results, &mu, &wg)
	}

	wg.Wait()

	file, err := os.Create("results_go.txt")
	if err != nil {
		log.Fatalf("Could not create output file: %v", err)
	}
	defer file.Close()

	for _, line := range results {
		_, err := file.WriteString(line + "\n")
		if err != nil {
			log.Printf("Error writing result: %v", err)
		}
	}

	fmt.Println("All tasks completed. Output saved to results_go.txt")
}
