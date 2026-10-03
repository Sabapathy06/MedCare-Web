import { motion } from 'framer-motion';
import { ShieldAlert, Phone, MapPin, Activity } from 'lucide-react';

const Emergency = () => {
  const container = {
    hidden: { opacity: 0 },
    show: { opacity: 1, transition: { staggerChildren: 0.1 } }
  };
  const item = { hidden: { opacity: 0, y: 20 }, show: { opacity: 1, y: 0 } };

  return (
    <motion.div initial="hidden" animate="show" exit="hidden" variants={container}>
      <div className="page-header" style={{ textAlign: 'center', marginBottom: '3rem' }}>
        <motion.h2 variants={item} style={{ color: 'var(--danger)' }}>Emergency SOS</motion.h2>
        <motion.p variants={item}>Tap the button below to instantly alert your emergency contacts and local services.</motion.p>
      </div>

      <motion.div variants={item} style={{ display: 'flex', justifyContent: 'center', marginBottom: '4rem' }}>
        <div style={{ 
          width: '250px', 
          height: '250px', 
          borderRadius: '50%', 
          background: 'rgba(239, 68, 68, 0.1)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          animation: 'pulse 2s infinite'
        }}>
          <button style={{ 
            width: '200px', 
            height: '200px', 
            borderRadius: '50%', 
            background: 'var(--danger)',
            color: 'white',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: '1rem',
            boxShadow: '0 0 40px rgba(239, 68, 68, 0.6)',
            transition: 'transform 0.1s',
            cursor: 'pointer'
          }}
          onMouseDown={(e) => e.currentTarget.style.transform = 'scale(0.95)'}
          onMouseUp={(e) => e.currentTarget.style.transform = 'scale(1)'}
          onMouseLeave={(e) => e.currentTarget.style.transform = 'scale(1)'}
          >
            <ShieldAlert size={64} />
            <span style={{ fontSize: '1.5rem', fontWeight: 700 }}>SOS</span>
          </button>
        </div>
      </motion.div>

      <div className="dashboard-grid">
        <motion.div variants={item} className="glass-card" style={{ padding: '1.5rem' }}>
          <h3 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Phone className="text-danger" />
            Emergency Contacts
          </h3>
          
          <div className="med-list">
            <div className="med-item" style={{ borderLeft: '4px solid var(--danger)' }}>
              <div className="med-info">
                <div className="med-details">
                  <h4>Local Emergency Services</h4>
                  <p>911</p>
                </div>
              </div>
              <button className="btn-secondary" style={{ color: 'var(--danger)', borderColor: 'rgba(239,68,68,0.3)' }}>
                Call Now
              </button>
            </div>
            
            <div className="med-item" style={{ borderLeft: '4px solid var(--warning)' }}>
              <div className="med-info">
                <div className="med-details">
                  <h4>Sarah (Daughter)</h4>
                  <p>+1 (555) 0198</p>
                </div>
              </div>
              <button className="btn-secondary">
                Call Now
              </button>
            </div>
          </div>
        </motion.div>

        <motion.div variants={item} className="glass-card" style={{ padding: '1.5rem' }}>
          <h3 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <MapPin className="text-primary" />
            Medical ID Broadcast
          </h3>
          
          <p style={{ color: 'var(--text-secondary)', marginBottom: '1rem' }}>
            When SOS is activated, the following information is securely sent to responders:
          </p>
          
          <div style={{ background: 'rgba(0,0,0,0.2)', padding: '1rem', borderRadius: 'var(--radius-md)' }}>
            <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Activity size={16} className="text-success" /> Current Location (GPS)</li>
              <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Activity size={16} className="text-success" /> Blood Type: O+</li>
              <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Activity size={16} className="text-success" /> Allergies: Penicillin</li>
              <li style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Activity size={16} className="text-success" /> Active Medications List</li>
            </ul>
          </div>
        </motion.div>
      </div>
      
      <style>{`
        @keyframes pulse {
          0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4); }
          70% { box-shadow: 0 0 0 40px rgba(239, 68, 68, 0); }
          100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
        }
      `}</style>
    </motion.div>
  );
};

export default Emergency;
