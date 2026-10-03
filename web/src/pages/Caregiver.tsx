import { motion } from 'framer-motion';
import { Users, UserPlus, Mail, Phone, Heart } from 'lucide-react';

const Caregiver = () => {
  const container = {
    hidden: { opacity: 0 },
    show: { opacity: 1, transition: { staggerChildren: 0.1 } }
  };
  const item = { hidden: { opacity: 0, y: 20 }, show: { opacity: 1, y: 0 } };

  return (
    <motion.div initial="hidden" animate="show" exit="hidden" variants={container}>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <motion.h2 variants={item}>Care Team</motion.h2>
          <motion.p variants={item}>Manage your caregivers and healthcare providers.</motion.p>
        </div>
        <motion.button variants={item} className="btn-primary">
          <UserPlus size={20} />
          <span>Invite Caregiver</span>
        </motion.button>
      </div>

      <div className="dashboard-grid">
        <motion.div variants={item} className="glass-card" style={{ padding: '2rem', textAlign: 'center' }}>
          <div className="avatar" style={{ width: '80px', height: '80px', margin: '0 auto 1.5rem', fontSize: '2rem', background: 'var(--success)' }}>
            SM
          </div>
          <h3 style={{ fontSize: '1.5rem', marginBottom: '0.25rem' }}>Sarah Miller</h3>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>Primary Caregiver (Daughter)</p>
          
          <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', marginBottom: '2rem' }}>
            <button className="btn-secondary" style={{ padding: '0.75rem', borderRadius: '50%' }}>
              <Phone size={20} />
            </button>
            <button className="btn-secondary" style={{ padding: '0.75rem', borderRadius: '50%' }}>
              <Mail size={20} />
            </button>
          </div>
          
          <div style={{ background: 'rgba(0,0,0,0.2)', padding: '1rem', borderRadius: 'var(--radius-md)', textAlign: 'left' }}>
            <h4 style={{ marginBottom: '0.5rem', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Permissions</h4>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
              <Heart size={16} className="text-primary" />
              <span>Full Medical Access</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <Heart size={16} className="text-primary" />
              <span>Can Edit Medications</span>
            </div>
          </div>
        </motion.div>

        <motion.div variants={item} className="glass-card" style={{ padding: '2rem', textAlign: 'center' }}>
          <div className="avatar" style={{ width: '80px', height: '80px', margin: '0 auto 1.5rem', fontSize: '2rem', background: 'var(--warning)' }}>
            DR
          </div>
          <h3 style={{ fontSize: '1.5rem', marginBottom: '0.25rem' }}>Dr. Roberts</h3>
          <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>Cardiologist</p>
          
          <div style={{ display: 'flex', justifyContent: 'center', gap: '1rem', marginBottom: '2rem' }}>
            <button className="btn-secondary" style={{ padding: '0.75rem', borderRadius: '50%' }}>
              <Phone size={20} />
            </button>
            <button className="btn-secondary" style={{ padding: '0.75rem', borderRadius: '50%' }}>
              <Mail size={20} />
            </button>
          </div>
          
          <div style={{ background: 'rgba(0,0,0,0.2)', padding: '1rem', borderRadius: 'var(--radius-md)', textAlign: 'left' }}>
            <h4 style={{ marginBottom: '0.5rem', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>Permissions</h4>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
              <Heart size={16} className="text-primary" />
              <span>Read-only Access</span>
            </div>
          </div>
        </motion.div>
      </div>
    </motion.div>
  );
};

export default Caregiver;
