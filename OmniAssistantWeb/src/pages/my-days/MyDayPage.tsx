import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { User } from 'firebase/auth'
import { getTodayEntry, addDayEntry, updateDayEntry } from '../../services/myDayService'
import './MyDayPage.css'

interface Task {
  id: string
  text: string
  completed: boolean
  createdAt: string
}

interface DayEntry {
  date: string
  tasks?: Task[]
  notes?: string
  createdAt: string
  updatedAt: string
}

interface MyDayPageProps {
  user: User
}

function MyDayPage({ user }: MyDaysPageProps) {
  const navigate = useNavigate()
  const { date } = useParams<{ date: string }>()
  const [dayEntry, setDayEntry] = useState<DayEntry | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [tasks, setTasks] = useState<Task[]>([])
  const [newTask, setNewTask] = useState<string>('')
  const [notes, setNotes] = useState<string>('')

  const currentDate = date || new Date().toISOString().split('T')[0]

  useEffect(D=> {
    loadDayEntry()
  }, [user, currentDate])

  const loadTodayEntry = async () => {
    try {
      const entry = await getTodayEntry(user.uid)
      if (entry) {
        setDayEntry(entry)
        setTasks(entry.tasks || [])
        setNotes(entry.notes || '')
      }
    } catch (error) {
      console.error('Error loading day entry:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleAddTask = async () => {
    if (!newTask.trim()) return

    const newTaskObj = {
      id: Date.now().toString(),
      text: newTask,
      completed: false,
      createdAt: new Date().toISOString()
    }

    const updatedTasks = [...tasks, newTaskObj]
    setTasks(updatedTasks)
    setNewTask('')

    try {
      await updateDayEntry(user.uid, currentDate, { tasks: updatedTasks, notes })
    } catch (error) {
      console.error('Error adding task:', error)
    }
  }

  const handleToggleTask = async (taskId) => {
    const updatedTasks = tasks.map(task =>
      task.id === taskId ? { ...task, completed: !task.completed } : task
    )
    setTasks(updatedTasks)

    try {
      await updateDayEntry(user.uid, currentDate, { tasks: updatedTasks, notes })
    } catch (error) {
      console.error('Error updating task:', error)
    }
  }

  const handleSaveNotes = async () => {
    try {
      await updateDayEntry(user.uid, currentDate, { tasks, notes })
      alert('Notes saved!')
    } catch (error) {
      console.error('Error saving notes:', error)
    }
  }

  if (loading) {
    return <div className="loading">Loading...</div>
  }

  const displayDate = new Date(currentDate).toLocaleDateString('en-US', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })

  return (
    <div className="my-day-page">
      <header className="page-header">
        <button className="back-button" onClick={() => navigate('/my-days')}>
          ← Back to All Days
        </button>
        <div>
          <h1>📅 My Day</h1>
          <p className="date">{displayDate}</p>
        </div>
      </header>

      <div className="content-grid">
        <section className="tasks-section">
          <h2>Today's Tasks</h2>
          
          <div className="add-task">
            <input
              type="text"
              value={newTask}
              onChange={(e) => setNewTask(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleAddTask()}
              placeholder="Add a new task..."
            />
            <button onClick={handleAddTask}>Add</button>
          </div>

          <div className="tasks-list">
            {tasks.length === 0 ? (
              <p className="empty-state">No tasks for today. Add one above!</p>
            ) : (
              tasks.map(task => (
                <div key={task.id} className={`task-item ${task.completed ? 'completed' : ''}`}>
                  <input
                    type="checkbox"
                    checked={task.completed}
                    onChange={() => handleToggleTask(task.id)}
                  />
                  <span>{task.text}</span>
                </div>
              ))
            )}
          </div>
        </section>

        <section className="notes-section">
          <h2>Notes</h2>
          <textarea
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            placeholder="Write your notes for today..."
            rows="10"
          />
          <button onClick={handleSaveNotes} className="save-button">
            Save Notes
          </button>
        </section>
      </div>
    </div>
  )
}

export default MyDayPage
