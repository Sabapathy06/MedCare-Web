import { motion } from 'framer-motion';
import { Pill, Activity, CalendarClock, CheckCircle2 } from 'lucide-react';

const Dashboard = () => {
  const container = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const item = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0 }
  };

  const todayDoses = [
    { id: 1, name: 'Lisinopril', dosage: '10mg', time: '08:00 AM', status: 'taken' },
    { id: 2, name: 'Metformin', dosage: '500mg', time: '01:00 PM', status: 'pending' },
    { id: 3, name: 'Atorvastatin', dosage: '20mg', time: '08:00 PM', status: 'pending' },
  ];

  return (
    <motion.div 
      initial="hidden"
      animate="show"
      exit="hidden"
      variants={container}
    >
      <div className="page-header">
        <motion.h2 variants={item}>Welcome back, John</motion.h2>
        <motion.p variants={item}>Here's your health summary for today.</motion.p>
      </div>

      <motion.div variants={item} className="dashboard-grid" style={{ marginBottom: '2rem' }}>
        <div className="stat-card glass-card">
          <div className="stat-header">
            <div className="stat-icon primary">
              <Pill size={24} />
            </div>
            <span className="badge pending">2 Pending</span>
          </div>
          <div>
            <div className="stat-value">1/3</div>
            <div className="stat-label">Doses Taken Today</div>
          </div>
        </div>

        <div className="stat-card glass-card">
          <div className="stat-header">
            <div className="stat-icon success">
              <Activity size={24} />
            </div>
            <span className="badge taken">Normal</span>
          </div>
          <div>
            <div className="stat-value">120/80</div>
            <div className="stat-label">Last Blood Pressure</div>
          </div>
        </div>

        <div className="stat-card glass-card">
          <div className="stat-header">
            <div className="stat-icon warning">
              <CalendarClock size={24} />
            </div>
          </div>
          <div>
            <div className="stat-value text-gradient">Tomorrow</div>
            <div className="stat-label">Next Appointment: Dr. Smith</div>
          </div>
        </div>
      </motion.div>

      <div className="dashboard-grid">
        <motion.div variants={item} className="glass-card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
            <h3>Today's Schedule</h3>
            <button className="text-primary" style={{ fontWeight: 500 }}>View All</button>
          </div>
          
          <div className="med-list">
            {todayDoses.map((dose) => (
              <div key={dose.id} className="med-item">
                <div className="med-info">
                  <div className="med-icon">
                    <Pill size={20} />
                  </div>
                  <div className="med-details">
                    <h4>{dose.name}</h4>
                    <p>{dose.dosage}</p>
                  </div>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <span className="med-time">{dose.time}</span>
                  {dose.status === 'taken' ? (
                    <CheckCircle2 size={24} className="text-success" />
                  ) : (
                    <button className="btn-primary" style={{ padding: '0.4rem 1rem', fontSize: '0.875rem' }}>
                      Take
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </motion.div>

        <motion.div variants={item} className="glass-card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1.5rem' }}>
            <h3>Care Team Updates</h3>
          </div>
          
          <div className="med-list">
            <div className="med-item">
              <div className="med-info">
                <div className="avatar" style={{ background: 'var(--success)' }}>
                  SM
                </div>
                <div className="med-details">
                  <h4>Sarah (Daughter)</h4>
                  <p>Checked your profile 2 hours ago</p>
                </div>
              </div>
            </div>
            
            <div className="med-item">
              <div className="med-info">
                <div className="avatar" style={{ background: 'var(--warning)' }}>
                  DR
                </div>
                <div className="med-details">
                  <h4>Dr. Roberts</h4>
                  <p>Updated your prescription</p>
                </div>
              </div>
            </div>
          </div>
        </motion.div>
      </div>
    </motion.div>
  );
};

export default Dashboard;
