import { BrowserRouter as Router, Routes, Route, Link, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Activity, Pill, Users, ShieldAlert, ScanLine, Settings, LayoutDashboard } from 'lucide-react';
import Dashboard from './pages/Dashboard';
import Medications from './pages/Medications';
import Caregiver from './pages/Caregiver';
import Emergency from './pages/Emergency';
import Scanner from './pages/Scanner';
import './App.css';

const Sidebar = () => {
  const location = useLocation();
  
  const navItems = [
    { path: '/', name: 'Dashboard', icon: LayoutDashboard },
    { path: '/medications', name: 'Medications', icon: Pill },
    { path: '/scanner', name: 'AI Scanner', icon: ScanLine },
    { path: '/caregiver', name: 'Care Team', icon: Users },
    { path: '/emergency', name: 'SOS', icon: ShieldAlert, danger: true },
  ];

  return (
    <nav className="sidebar glass">
      <div className="logo-container">
        <Activity size={32} className="text-primary" />
        <h1 className="text-gradient">MedCare</h1>
      </div>
      
      <div className="nav-links">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.path;
          
          return (
            <Link 
              key={item.path} 
              to={item.path}
              className={`nav-item ${isActive ? 'active' : ''} ${item.danger ? 'danger' : ''}`}
            >
              <Icon size={20} />
              <span>{item.name}</span>
              {isActive && (
                <motion.div 
                  layoutId="active-indicator"
                  className="active-indicator"
                  initial={false}
                  transition={{ type: "spring", stiffness: 300, damping: 30 }}
                />
              )}
            </Link>
          );
        })}
      </div>
      
      <div className="user-profile">
        <div className="avatar">JD</div>
        <div className="user-info">
          <h4>John Doe</h4>
          <p>Patient ID: MC-8492</p>
        </div>
        <Settings size={20} className="text-secondary" style={{ cursor: 'pointer' }} />
      </div>
    </nav>
  );
};

const Header = () => {
  return (
    <header className="top-header glass">
      <div className="search-bar">
        <input type="text" placeholder="Search medications, contacts..." />
      </div>
      <div className="header-actions">
        <button className="btn-secondary">
          <Pill size={18} />
          <span>Refill Request</span>
        </button>
        <button className="btn-primary">
          <ScanLine size={18} />
          <span>Scan Meds</span>
        </button>
      </div>
    </header>
  );
};

function App() {
  return (
    <Router>
      <div className="app-container">
        <Sidebar />
        <main className="main-content">
          <Header />
          <div className="page-wrapper">
            <AnimatePresence mode="wait">
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/medications" element={<Medications />} />
                <Route path="/scanner" element={<Scanner />} />
                <Route path="/caregiver" element={<Caregiver />} />
                <Route path="/emergency" element={<Emergency />} />
              </Routes>
            </AnimatePresence>
          </div>
        </main>
      </div>
    </Router>
  );
}

export default App;
