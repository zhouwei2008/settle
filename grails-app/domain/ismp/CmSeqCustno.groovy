package ismp

class CmSeqCustno {

  static mapping = {
    id generator: 'sequence', params: [sequence: 'seq_cm_custno']
  }
  static constraints = {
  }
}
